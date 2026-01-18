package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

// https://github.com/InLieuOfLuna/elytra-recast/
public class ElytraBounce extends Module {

    public ElytraBounce() {
        super("conejo", "haz el elytra-bounce manteniendo el espacio", Category.MOVEMENT);
    }

    public boolean bounce() {
        if (canUseElytra() && canBounce()) {
            if (mc.getNetworkHandler() != null)
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                        mc.player,
                        ClientCommandC2SPacket
                                .Mode.START_FALL_FLYING)
                );
            return true;

        } else {
            return false;
        }
    }

    public boolean canUseElytra() {
        if (mc.player.input.playerInput.jump()
                && !mc.player.getAbilities().flying
                && !mc.player.hasVehicle()
                && !mc.player.isClimbing()) {
            ItemStack stack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            return stack.isOf(Items.ELYTRA) && LivingEntity.canGlideWith(stack, EquipmentSlot.CHEST);
        } else {
            return false;
        }
    }

    public boolean canBounce() {
        if (!mc.player.isTouchingWater() && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack stack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            if (stack.isOf(Items.ELYTRA) && LivingEntity.canGlideWith(stack, EquipmentSlot.CHEST)) {
                mc.player.startGliding();
                return true;
            } else
                return false;
        } else
            return false;
    }
}