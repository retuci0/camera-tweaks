package me.retucio.sputnik.mixin.mixins.entity;

import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.movement.ElytraBounce;
import me.retucio.sputnik.module.modules.movement.Step;
import me.retucio.sputnik.module.modules.movement.Velocity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
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
    public abstract boolean isFallFlying();

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void recastIfLanded(CallbackInfo ci) {
        ElytraBounce ebounce = ModuleManager.INSTANCE.getModuleByClass(ElytraBounce.class);

        if (!((Object) this instanceof LocalPlayer)
                || mc.player == null
                || ebounce == null)
            return;

        boolean elytra = isFallFlying();

        if (awaitingElytra) {
            if (elytra) awaitingElytra = false;
        } else if (!elytra && prevElytra) {
            mc.getSoundManager().stop(SoundEvents.ELYTRA_FLYING.location(), SoundSource.PLAYERS);  // armor equip elytra??
            ebounce.bounce();
            awaitingElytra = ebounce.canUseElytra();
        }

        prevElytra = elytra;
    }


    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void step(CallbackInfoReturnable<Float> cir) {
        Step step = ModuleManager.INSTANCE.getModuleByClass(Step.class);
        if (step.isEnabled()) cir.setReturnValue(step.height.getFloatValue());
    }

    @Inject(method = "knockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V"), cancellable = true)
    private void onTakeKb(double strength, double x, double z, CallbackInfo ci, @Local(name = "deltaMovement") Vec3 dm, @Local(name = "deltaVector") Vec3 dv) {
        Velocity velocity = ModuleManager.INSTANCE.getModuleByClass(Velocity.class);
        if (!velocity.isEnabled() || velocity.hits.getValue()) return;
        boolean onGround = ((LivingEntity) (Object) this).onGround();

        double kbX = (velocity.xPercentage.getValue() / 100) * (dm.x / 2 - dv.x);
        double kbY = (velocity.yPercentage.getValue() / 100) * ((onGround ? Math.min(0.4, dm.y / 2 + strength) : dm.y));
        double kbZ = (velocity.zPercentage.getValue() / 100) * (dm.z / 2 - dv.z);

        ci.cancel();
        ((Entity) (Object) this).addDeltaMovement(new Vec3(kbX, kbY, kbZ));
    }
}