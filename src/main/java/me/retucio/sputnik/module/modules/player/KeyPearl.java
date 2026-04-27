package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;


public class KeyPearl extends Module {

    private final BooleanSetting swapBack = sgGeneral.add(new BooleanSetting(
            "cambiar de vuelta",
            "cambiar el ítem de vuelta a su lugar o seleccionar el slot original",
            true
    ));

    public KeyPearl() {
        super("tecla de perla", "lanza una perla con una tecla", Category.PLAYER);
        keyMode.setValue(KeyModes.HOLD);
    }

    private int prevSlot = -1;
    private int prevStackSlot = -1;
    private boolean didMoveItem = false;

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        switchToPearl();
        super.onEnable();
    }

    @Override
    // al desactivar para poder prever la trayectoria si trayectorias está activado
    public void onDisable() {
        throwPearl();
        switchBack();
        prevSlot = -1;
        prevStackSlot = -1;
        didMoveItem = false;
        super.onDisable();
    }

    private void switchToPearl() {
        Inventory inv = mc.player.getInventory();

        int slot = InventoryUtil.findSlot(stack -> stack.is(Items.ENDER_PEARL));
        if (slot < 0) {
            ChatUtil.error("no tienes perlas");
            toggle();
            return;
        }

        if (Inventory.isHotbarSlot(slot)) {
            prevSlot = inv.getSelectedSlot();
            inv.setSelectedSlot(slot);
            return;
        }

        InventoryUtil.swapWithHotbar(slot, inv.getSelectedSlot());
        didMoveItem = true;
        prevStackSlot = slot;
    }

    private void throwPearl() {
        if (!mc.player.getMainHandItem().is(Items.ENDER_PEARL)) return;
        mc.startUseItem();
    }

    private void switchBack() {
        if (!swapBack.getValue()) return;
        if (didMoveItem) {
            InventoryUtil.swapWithHotbar(prevStackSlot, mc.player.getInventory().getSelectedSlot());
        } else {
            mc.player.getInventory().setSelectedSlot(prevSlot);
        }
    }
}