package me.retucio.sputnik.mixin;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.movement.ElytraBounce;
import me.retucio.sputnik.module.modules.movement.Step;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
}