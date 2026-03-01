package me.retucio.sputnik.mixin.mixins.entity;

import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.movement.ElytraBounce;
import me.retucio.sputnik.module.modules.movement.Step;
import me.retucio.sputnik.module.modules.movement.Velocity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.sputnik.Sputnik.mc;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique private boolean prevElytra = false;
    @Unique private boolean awaitingElytra = false;

    @Shadow
    public abstract boolean isGliding();

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void recastIfLanded(CallbackInfo ci) {
        ElytraBounce bounce = ModuleManager.INSTANCE.getModuleByClass(ElytraBounce.class);

        if (!((Object) this instanceof ClientPlayerEntity)
                || mc.player == null
                || bounce == null)
            return;

        boolean elytra = isGliding();

        if (awaitingElytra) {
            if (elytra) awaitingElytra = false;

        } else if (!elytra && prevElytra) {
            mc.getSoundManager().stopSounds(SoundEvents.ITEM_ELYTRA_FLYING.id(), SoundCategory.PLAYERS);
            bounce.bounce();
            awaitingElytra = bounce.canUseElytra();
        }

        prevElytra = elytra;
    }


    @Inject(method = "getStepHeight", at = @At("RETURN"), cancellable = true)
    private void step(CallbackInfoReturnable<Float> cir) {
        Step step = ModuleManager.INSTANCE.getModuleByClass(Step.class);
        if (step.isEnabled()) cir.setReturnValue(step.height.getFloatValue());
    }

    @Inject(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(DDD)V"), cancellable = true)
    private void onTakeKb(double strength, double x, double z, CallbackInfo ci, @Local(ordinal = 0) Vec3d vec3d, @Local(ordinal = 1) Vec3d vec3d2) {
        Velocity velocity = ModuleManager.INSTANCE.getModuleByClass(Velocity.class);
        if (!velocity.isEnabled() || velocity.hits.getValue()) return;
        boolean onGround = ((LivingEntity) (Object) this).isOnGround();

        double kbX = (velocity.xPercentage.getValue() / 100) * (vec3d.x / 2 - vec3d2.x);
        double kbY = (velocity.yPercentage.getValue() / 100) * ((onGround ? Math.min(0.4, vec3d.y / 2 + strength) : vec3d.y));
        double kbZ = (velocity.zPercentage.getValue() / 100) * (vec3d.z / 2 - vec3d2.z);

        ci.cancel();
        ((Entity) (Object) this).addVelocity(kbX, kbY, kbZ);
    }
}