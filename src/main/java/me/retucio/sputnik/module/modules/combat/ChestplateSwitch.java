package me.retucio.sputnik.module.modules.combat;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.interact.AttackEntityEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


/**
 * @author retucio
 */

public class ChestplateSwitch extends Module {

    private final BooleanSetting switchBack = sgGeneral.add(new BooleanSetting(
            "cambiar de vuelta", "cambiar de vuelta a elytras, si estaban puestas", true
    ));

    public ChestplateSwitch() {
        super("autopechera", "cambia a pechera antes de pegar con el mazo", Category.COMBAT);
    }

    @EventListener
    private void onAttack(AttackEntityEvent event) {
        if (!mc.player.getMainHandItem().is(Items.MACE)) return;
        if (mc.player.hasItemInSlot(EquipmentSlot.CHEST) || mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            int slot = InventoryUtil.findSlot(this::isChestplate);
            if (!Inventory.isHotbarSlot(slot)) {
                ChatUtil.error("no se encontró una pechera en la hotbar");
                return;
            }

            event.cancel();

            int prevSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(slot);
            mc.startUseItem();
            mc.player.getInventory().setSelectedSlot(prevSlot);

            mc.getConnection().send(new ServerboundAttackPacket(event.getEntity().getId()));
        }
    }

    private boolean isChestplate(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.CHAINMAIL_CHESTPLATE
            || item == Items.COPPER_CHESTPLATE
            || item == Items.DIAMOND_CHESTPLATE
            || item == Items.GOLDEN_CHESTPLATE
            || item == Items.IRON_CHESTPLATE
            || item == Items.LEATHER_CHESTPLATE
            || item == Items.NETHERITE_CHESTPLATE;
    }
}
