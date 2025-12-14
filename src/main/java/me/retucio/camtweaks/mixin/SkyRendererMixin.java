package me.retucio.camtweaks.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.NoRender;
import me.retucio.camtweaks.module.modules.PerspectivePlus;
import me.retucio.camtweaks.module.modules.TimeChanger;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private void onRenderMoon(int phase, float alpha, MatrixStack matrixStack, CallbackInfo ci) {
        if (timeChanger.isEnabled() && !timeChanger.renderMoon.isEnabled()) ci.cancel();
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