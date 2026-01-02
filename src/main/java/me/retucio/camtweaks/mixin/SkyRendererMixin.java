package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.render.NoRender;
import me.retucio.camtweaks.module.modules.world.TimeChanger;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRendering.class)
public abstract class SkyRendererMixin {

    @Unique
    TimeChanger timeChanger;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        timeChanger = ModuleManager.INSTANCE.getModuleByClass(TimeChanger.class);
    }

    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
    private void onRenderSun(float alpha, MatrixStack matrices, CallbackInfo ci) {
        if (timeChanger.isEnabled() && !timeChanger.renderSun.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
    private void onRenderMoon(MoonPhase moonPhase, float alpha, MatrixStack matrices, CallbackInfo ci) {
        if (!timeChanger.isEnabled()) return;
        if (!timeChanger.renderMoon.isEnabled()) ci.cancel();
    }

    @ModifyArg(method = "renderCelestialBodies", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SkyRendering;renderMoon(Lnet/minecraft/world/MoonPhase;FLnet/minecraft/client/util/math/MatrixStack;)V"))
    private MoonPhase timeChangerMoonPhase(MoonPhase original) {
        if (!timeChanger.isEnabled() || timeChanger.moonPhase.is(TimeChanger.MoonPhases.DEFAULT)) return original;
        int phase = timeChanger.moonPhase.getIndex();
        if (timeChanger.moonPhase.is(TimeChanger.MoonPhases.DEFAULT)) phase = original.getIndex();
        return MoonPhase.values()[phase];
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    private void onRenderStars(float brightness, MatrixStack matrices, CallbackInfo ci) {
        if (timeChanger.isEnabled() && !timeChanger.renderStars.isEnabled()) ci.cancel();
    }

    @Inject(method = "drawEndLightFlash", at = @At("HEAD"), cancellable = true)
    private void onEndFlash(MatrixStack matrixStack, float f, float skyFactor, float pitch, CallbackInfo ci) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (noRender.isEnabled() && !noRender.endFlashes.isEnabled()) ci.cancel();
    }
}