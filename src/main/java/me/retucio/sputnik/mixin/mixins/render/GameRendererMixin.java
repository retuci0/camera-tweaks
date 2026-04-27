package me.retucio.sputnik.mixin.mixins.render;


import me.retucio.sputnik.event.render.GetFOVEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.misc.UnfocusedCpu;
import me.retucio.sputnik.module.modules.render.NoRender;
import me.retucio.sputnik.module.modules.camera.Zoom;


import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.ModelManager;
import org.joml.Matrix4fc;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;
import static me.retucio.sputnik.Sputnik.mc;


@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique Zoom zoom;
    @Unique Freecam freecam;
    @Unique NoRender noRender;

    @Shadow @Final
    private Minecraft minecraft;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(Minecraft minecraft, ItemInHandRenderer itemInHandRenderer, RenderBuffers renderBuffers, ModelManager modelManager, CallbackInfo ci) {
        zoom = ModuleManager.INSTANCE.getModuleByClass(Zoom.class);;
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void avoidRenderingUnfocusedCpu(DeltaTracker dt, boolean tick, CallbackInfo ci) {
        if (ModuleManager.INSTANCE.getModuleByClass(UnfocusedCpu.class).isEnabled() && !mc.isWindowActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(CameraRenderState cameraState, float deltaPartialTick, Matrix4fc modelViewMatrix, CallbackInfo ci) {
        if ((zoom.isEnabled() && !zoom.showHands.getValue())
                || (freecam.isEnabled() && !freecam.renderHands.getValue()))
            ci.cancel();
    }

    @ModifyVariable(method = "renderLevel", at = @At(value = "STORE"), name = "skew")
    private float noRenderNauseaDistortion(float scaledNauseaEffectFactor) {
        return (noRender.isEnabled() && !noRender.nauseaEffect.getValue()) ? 0 : scaledNauseaEffectFactor;
    }
}
