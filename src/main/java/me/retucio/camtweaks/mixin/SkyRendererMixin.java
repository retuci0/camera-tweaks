package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.PerspectivePlus;
import me.retucio.camtweaks.module.modules.TimeChanger;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
    private void onRenderSun(float alpha, VertexConsumerProvider vertexConsumers, MatrixStack matrices, CallbackInfo ci) {
        if (timeChanger.isEnabled() && !timeChanger.renderSun.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
    private void onRenderMoon(int phase, float alpha, VertexConsumerProvider vertexConsumers, MatrixStack matrices, CallbackInfo ci) {
        if (timeChanger.isEnabled() && !timeChanger.renderMoon.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    private void onRenderStars(float brightness, MatrixStack matrices, CallbackInfo ci) {
        if (timeChanger.isEnabled() && !timeChanger.renderStars.isEnabled()) ci.cancel();
    }
}