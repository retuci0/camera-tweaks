package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.NoRender;
import net.minecraft.particle.EntityEffectParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityEffectParticleEffect.class)
public abstract class EntityEffectParticleEffectMixin {

    @Inject(method = "getAlpha", at = @At("HEAD"), cancellable = true)
    private void noRenderPotionParticles(CallbackInfoReturnable<Float> cir) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (noRender.isEnabled()) cir.setReturnValue(256 - noRender.potionParticleAlpha.getFloatValue());
    }
}