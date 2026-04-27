package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


// https://github.com/InLieuOfLuna/elytra-recast/
public class ElytraBounce extends Module {

    public ElytraBounce() {
        super("conejo", "haz el elytra-bounce manteniendo el espacio", Category.MOVEMENT);
    }

    public boolean bounce() {
        if (canUseElytra() && canBounce()) {
            if (mc.getConnection() != null)
                mc.getConnection().send(new ServerboundPlayerCommandPacket(
                        mc.player,
                        ServerboundPlayerCommandPacket
                                .Action.START_FALL_FLYING
                ));
            return true;
        } else {
            return false;
        }
    }

    public boolean canUseElytra() {
        if (mc.player.input.keyPresses.jump()
                && !mc.player.getAbilities().flying
                && !mc.player.isPassenger()
                && !mc.player.onClimbable()) {
            ItemStack stack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            return stack.is(Items.ELYTRA) && LivingEntity.canGlideUsing(stack, EquipmentSlot.CHEST);
        } else {
            return false;
        }
    }

    public boolean canBounce() {
        if (!mc.player.isInWater() && !mc.player.hasEffect(MobEffects.LEVITATION)) {
            ItemStack stack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            if (stack.is(Items.ELYTRA) && LivingEntity.canGlideUsing(stack, EquipmentSlot.CHEST)) {
                mc.player.startFallFlying();
                return true;
            } else
                return false;
        } else
            return false;
    }
}