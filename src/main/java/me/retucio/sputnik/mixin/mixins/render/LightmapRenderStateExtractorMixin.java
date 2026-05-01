package me.retucio.sputnik.mixin.mixins.render;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Fullbright;
import me.retucio.sputnik.module.modules.render.NoRender;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {

    @Unique NoRender noRender;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Inject(method = "calculateDarknessScale", at = @At("HEAD"), cancellable = true)
    private void noRenderDarkness(LivingEntity camera, float darknessGamma, float partialTickTime, CallbackInfoReturnable<Float> cir) {
        if (noRender.isEnabled() && !noRender.darknessEffect.getValue()) cir.setReturnValue(0f);
    }
}