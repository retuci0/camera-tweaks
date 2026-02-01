package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.module.setting.settings.OptionSetting;
import me.retucio.sputnik.util.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Offhand extends Module {

    private final OptionSetting<Item> item = sgGeneral.add(new OptionSetting<>(
            "ítem", "ítem a equipar",
            Lists.itemList, Items.TOTEM_OF_UNDYING, Lists.itemNames
    ));

    private final NumberSetting delaySetting = sgGeneral.add(new NumberSetting(
            "delay",
            "delay del cambiazo (en ticks)",
            0, 0, 20, 1
    ));

    private final BooleanSetting override = sgGeneral.add(new BooleanSetting(
            "anular",
            "ignora que ya haya un ítem en la mano secundaria",
            true
    ));

    private int delay;
    private boolean holdingItem;

    public Offhand() {
        super("mano secundaria",
                "automáticamente equipa un ítem en la mano secundaria",
                Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        if (holdingItem && mc.player.getOffHandStack().getItem() != item.getValue())
            delay = Math.max(delaySetting.getIntValue(), delay);

        holdingItem = mc.player.getOffHandStack().getItem() == item.getValue();

        if (delay > 0) {
            delay--;
            return;
        }

        if (holdingItem || (!mc.player.getOffHandStack().isEmpty()
                && !override.getValue()))
            return;

        if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
            for (int i = 9; i < 45; i++) {

                if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem() == item.getValue()) {
                    boolean itemInOffhand = !mc.player.getOffHandStack().isEmpty();

                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);

                    if (itemInOffhand)
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);

                    delay = delaySetting.getIntValue();
                    return;
                }
            }
        } else {
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == item.getValue()) {
                    if (i != mc.player.getInventory().getSelectedSlot()) {
                        mc.player.getInventory().setSelectedSlot(i);
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                    }

                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                            BlockPos.ORIGIN,
                            Direction.DOWN));
                    delay = delaySetting.getIntValue();
                    return;
                }
            }
        }
    }
}