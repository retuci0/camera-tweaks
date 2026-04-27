package me.retucio.sputnik.module.modules.inventory;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.module.setting.settings.OptionSetting;
import me.retucio.sputnik.util.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * @author retucio
 */

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
                Category.INVENTORY);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;

        if (holdingItem && mc.player.getOffhandItem().getItem() != item.getValue())
            delay = Math.max(delaySetting.getIntValue(), delay);

        holdingItem = mc.player.getOffhandItem().getItem() == item.getValue();

        if (delay > 0) {
            delay--;
            return;
        }

        if (holdingItem || (!mc.player.getOffhandItem().isEmpty()
                && !override.getValue()))
            return;

        if (mc.player.containerMenu == mc.player.inventoryMenu) {
            for (int i = 9; i < 45; i++) {

                if (mc.player.getInventory().getItem(i >= 36 ? i - 36 : i).getItem() == item.getValue()) {
                    boolean itemInOffhand = !mc.player.getOffhandItem().isEmpty();

                    mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, i, 0, ContainerInput.PICKUP, mc.player);
                    mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, 45, 0, ContainerInput.PICKUP, mc.player);

                    if (itemInOffhand)
                        mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, i, 0, ContainerInput.PICKUP, mc.player);

                    delay = delaySetting.getIntValue();
                    return;
                }
            }
        } else {
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getItem(i).getItem() == item.getValue()) {
                    if (i != mc.player.getInventory().getSelectedSlot()) {
                        mc.player.getInventory().setSelectedSlot(i);
                        mc.player.connection.send(new ClientboundSetHeldSlotPacket(i));
                    }

                    mc.player.connection.send(new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                            BlockPos.ZERO,
                            Direction.DOWN));
                    delay = delaySetting.getIntValue();
                    return;
                }
            }
        }
    }
}