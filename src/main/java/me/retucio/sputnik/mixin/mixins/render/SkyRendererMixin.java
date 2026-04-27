package me.retucio.sputnik.mixin.mixins.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.NoRender;
import me.retucio.sputnik.module.modules.world.TimeChanger;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @Unique
    TimeChanger timeChanger;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        timeChanger = ModuleManager.INSTANCE.getModuleByClass(TimeChanger.class);
    }

    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
    private void onRenderSun(float rainBrightness, PoseStack poseStack, CallbackInfo ci) {
        if (timeChanger.isEnabled() && !timeChanger.renderSun.getValue()) ci.cancel();
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
    private void onRenderMoon(MoonPhase moonPhase, float rainBrightness, PoseStack poseStack, CallbackInfo ci) {
        if (!timeChanger.isEnabled()) return;
        if (!timeChanger.renderMoon.getValue()) ci.cancel();
    }

    @ModifyArg(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private MoonPhase timeChangerMoonPhase(MoonPhase original) {
        if (!timeChanger.isEnabled() || timeChanger.moonPhase.is(TimeChanger.MoonPhases.DEFAULT)) return original;
        int phase = timeChanger.moonPhase.getIndex();
        if (timeChanger.moonPhase.is(TimeChanger.MoonPhases.DEFAULT)) phase = original.index();
        return MoonPhase.values()[phase];
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    private void onRenderStars(float starBrightness, PoseStack poseStack, CallbackInfo ci) {
        if (timeChanger.isEnabled() && !timeChanger.renderStars.getValue()) ci.cancel();
    }

    @Inject(method = "renderEndFlash", at = @At("HEAD"), cancellable = true)
    private void onEndFlash(PoseStack matrices, float intensity, float xAngle, float yAngle, CallbackInfo ci) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (noRender.isEnabled() && !noRender.endFlashes.getValue()) ci.cancel();
    }
}