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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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

        InteractionHand hand = event.getHand();
        int usedSlot = getSlotFromHand(hand);
        if (usedSlot == -1) return;

        ItemStack usedStack = hand == InteractionHand.MAIN_HAND ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
        if (usedStack.isEmpty()) return;

        if (excludedItems.isEnabled(usedStack.getItem())) return;
        if (!unstackable.getValue() && usedStack.getMaxStackSize() == 1) return;
        if (!offhand.getValue() && hand == InteractionHand.OFF_HAND) return;

        pendingTasks.remove(usedSlot);
        pendingTasks.put(usedSlot, new ReplenishTask(usedSlot, usedStack.getItem(), tickDelay.getIntValue()));
    }

    @EventListener
    private void onDisconnect(DisconnectEvent event) {
        pendingTasks.clear();
    }

    private void executeReplenish(int slot, Item item) {
        if (mc.player == null) return;
        Inventory inv = mc.player.getInventory();

        ItemStack currentStack = inv.getItem(slot);
        if (currentStack.isEmpty() || !currentStack.is(item)) {
            return;
        }

        if (currentStack.getCount() > minCount.getIntValue()) {
            return;
        }

        List<Integer> excludeSlots = new ArrayList<>();
        excludeSlots.add(slot);
        if (!offhand.getValue()) {
            excludeSlots.add(InventoryUtil.OFFHAND_SLOT);
        }

        InventoryMenu handler = mc.player.inventoryMenu;
        int sourceContainerSlot = -1;

        for (int playerIndex = 0; playerIndex <= 40; playerIndex++) {
            if (excludeSlots.contains(playerIndex)) continue;

            ItemStack stack = inv.getItem(playerIndex);
            if (!stack.isEmpty() && stack.is(item) && ItemStack.isSameItemSameComponents(stack, currentStack)) {
                int containerSlot = InventoryUtil.getContainerSlotByPlayerIndex(handler, playerIndex);
                if (containerSlot != -1) {
                    sourceContainerSlot = containerSlot;
                    break;
                }
            }
        }

        if (sourceContainerSlot == -1) return;

        int targetContainerSlot = InventoryUtil.getContainerSlotByPlayerIndex(handler, slot);
        if (targetContainerSlot == -1) return;

        InventoryUtil.swapSlots(sourceContainerSlot, targetContainerSlot, handler);
    }

    private int getSlotFromHand(InteractionHand hand) {
        if (mc.player == null) return -1;
        if (hand == InteractionHand.MAIN_HAND) {
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
