package me.retucio.sputnik.module.modules.inventory;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.interact.UseItemEvent;
import me.retucio.sputnik.event.network.DisconnectEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.InventoryUtil;
import me.retucio.sputnik.util.Lists;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Hand;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Replenish extends Module {

    private final NumberSetting minCount = sgGeneral.add(new NumberSetting(
            "mínimo de items",
            "límite de cantidad de items para empezar a reponer",
            5,
            1,
            64,
            1
    ));

    private final NumberSetting tickDelay = sgGeneral.add(new NumberSetting(
            "delay",
            "delay al reponer, en ticks",
            5,
            0,
            20,
            1
    ));

    private final BooleanSetting offhand = sgGeneral.add(new BooleanSetting(
            "mano secundaria",
            "reponer la mano secundaria también",
            true
    ));

    private final BooleanSetting unstackable = sgGeneral.add(new BooleanSetting(
            "no stackeables",
            "reponer items no stackeables",
            true
    ));

    private final ListSetting<Item> excludedItems = sgGeneral.add(new ListSetting<>(
            "items excluídos",
            "items que no se reponen",
            Lists.itemList,
            Lists.allFalse(Lists.itemList),
            Lists.itemNames
    ));

    private static final Map<Integer, ReplenishTask> pendingTasks = new ConcurrentHashMap<>();

    public Replenish() {
        super("reponer",
                "repone items a medida que se consumen de manera automática", Category.INVENTORY);
    }

    @Override
    public void onTick() {
        if (pendingTasks.isEmpty()) return;
        if (mc.player == null) {
            pendingTasks.clear();
            return;
        }

        Iterator<Map.Entry<Integer, ReplenishTask>> iterator = pendingTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ReplenishTask> entry = iterator.next();
            ReplenishTask task = entry.getValue();
            task.tick();

            if (task.ticksLeft <= 0) {
                iterator.remove();
                executeReplenish(task.slot, task.item);
            }
        }
    }

    @EventListener
    private void onUseItem(UseItemEvent event) {
        if (mc.player == null) return;

        Hand hand = event.getHand();
        int usedSlot = getSlotFromHand(hand);
        if (usedSlot == -1) return;

        ItemStack usedStack = hand == Hand.MAIN_HAND ? mc.player.getMainHandStack() : mc.player.getOffHandStack();
        if (usedStack.isEmpty()) return;

        if (excludedItems.isEnabled(usedStack.getItem())) return;
        if (!unstackable.getValue() && usedStack.getMaxCount() == 1) return;
        if (!offhand.getValue() && hand == Hand.OFF_HAND) return;

        pendingTasks.remove(usedSlot);
        pendingTasks.put(usedSlot, new ReplenishTask(usedSlot, usedStack.getItem(), tickDelay.getIntValue()));
    }

    @EventListener
    private void onDisconnect(DisconnectEvent event) {
        pendingTasks.clear();
    }

    private void executeReplenish(int slot, Item item) {
        if (mc.player == null) return;
        PlayerInventory inv = mc.player.getInventory();

        // Get current stack in the slot
        ItemStack currentStack = inv.getStack(slot);
        if (currentStack.isEmpty() || !currentStack.isOf(item)) {
            return; // item changed or slot empty – abort
        }

        // Check threshold
        if (currentStack.getCount() > minCount.getIntValue()) {
            return; // still above threshold
        }

        // Find another stack of the same item in the inventory (excluding the used slot and possibly offhand)
        List<Integer> excludeSlots = new ArrayList<>();
        excludeSlots.add(slot);
        if (!offhand.getValue()) {
            excludeSlots.add(InventoryUtil.OFFHAND_SLOT);
        }

        PlayerScreenHandler handler = mc.player.playerScreenHandler;
        int sourceContainerSlot = -1;

        // Iterate over all player inventory slots (0-40)
        for (int playerIndex = 0; playerIndex <= 40; playerIndex++) {
            if (excludeSlots.contains(playerIndex)) continue;

            ItemStack stack = inv.getStack(playerIndex);
            if (!stack.isEmpty() && stack.isOf(item) && ItemStack.areItemsAndComponentsEqual(stack, currentStack)) {
                // Found a match, convert to container slot
                int containerSlot = InventoryUtil.getContainerSlotByPlayerIndex(handler, playerIndex);
                if (containerSlot != -1) {
                    sourceContainerSlot = containerSlot;
                    break;
                }
            }
        }

        if (sourceContainerSlot == -1) return; // no source found

        // Target container slot is the used slot
        int targetContainerSlot = InventoryUtil.getContainerSlotByPlayerIndex(handler, slot);
        if (targetContainerSlot == -1) return;

        InventoryUtil.swapSlots(sourceContainerSlot, targetContainerSlot, handler);
    }

    private int getSlotFromHand(Hand hand) {
        if (mc.player == null) return -1;
        if (hand == Hand.MAIN_HAND) {
            return mc.player.getInventory().getSelectedSlot();
        } else {
            return InventoryUtil.OFFHAND_SLOT;  // 40
        }
    }

    private static class ReplenishTask {
        final int slot;
        final Item item;
        int ticksLeft;

        ReplenishTask(int slot, Item item, int delay) {
            this.slot = slot;
            this.item = item;
            this.ticksLeft = delay;
        }

        void tick() {
            ticksLeft--;
        }
    }


}
