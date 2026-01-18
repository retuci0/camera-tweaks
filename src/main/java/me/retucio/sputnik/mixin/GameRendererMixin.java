package me.retucio.sputnik.mixin;


import me.retucio.sputnik.event.events.GetFOVEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.misc.UnfocusedCpu;
import me.retucio.sputnik.module.modules.render.NoRender;
import me.retucio.sputnik.module.modules.camera.Zoom;
import me.retucio.sputnik.util.interfaces.IVec3d;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.entity.Entity;

import org.joml.Matrix4f;
import org.joml.Vector3d;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;
import static me.retucio.sputnik.Sputnik.mc;


@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique
    boolean freecamDone;

    @Unique
    Zoom zoom;

    @Unique
    Freecam freecam;

    @Unique
    NoRender noRender;

    @Shadow @Final
    private MinecraftClient client;

    @Shadow
    public abstract void updateCrosshairTarget(float tickDelta);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(MinecraftClient client, HeldItemRenderer firstPersonHeldItemRenderer, BufferBuilderStorage buffers, BlockRenderManager blockRenderManager, CallbackInfo ci) {
        zoom = ModuleManager.INSTANCE.getModuleByClass(Zoom.class);;
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float modifyFov(float original) {
        return EVENT_BUS.post(new GetFOVEvent(original)).getFov();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void avoidRenderingUnfocusedCpu(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (ModuleManager.INSTANCE.getModuleByClass(UnfocusedCpu.class).isEnabled() && !mc.isWindowFocused())
            ci.cancel();
    }

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo ci) {
        if ((freecam.isEnabled()) && client.getCameraEntity() != null && !freecamDone) {
            ci.cancel();

            Entity cameraEntity = client.getCameraEntity();
            Vector3d pos = freecam.getPos();
            Vector3d prevPos = freecam.getPrevPos();

            double x = cameraEntity.getX();
            double y = cameraEntity.getY();
            double z = cameraEntity.getZ();
            double lastX = cameraEntity.lastX;
            double lastY = cameraEntity.lastY;
            double lastZ = cameraEntity.lastZ;
            float yaw = cameraEntity.getYaw();
            float pitch = cameraEntity.getPitch();
            float lastYaw = cameraEntity.lastYaw;
            float lastPitch = cameraEntity.lastPitch;

            ((IVec3d) cameraEntity.getEntityPos()).sputnik$set(pos.x, pos.y - cameraEntity.getEyeHeight(cameraEntity.getPose()), pos.z);
            cameraEntity.lastX = prevPos.x;
            cameraEntity.lastY = prevPos.y - cameraEntity.getEyeHeight(cameraEntity.getPose());
            cameraEntity.lastZ = prevPos.z;
            cameraEntity.setYaw(freecam.getYaw());
            cameraEntity.setPitch(freecam.getPitch());
            cameraEntity.lastYaw = freecam.getPrevYaw();
            cameraEntity.lastPitch = freecam.getPrevPitch();

            freecamDone = true;
            updateCrosshairTarget(tickDelta);
            freecamDone = false;

            ((IVec3d) cameraEntity.getEntityPos()).sputnik$set(x, y, z);
            cameraEntity.lastX = lastX;
            cameraEntity.lastY = lastY;
            cameraEntity.lastZ = lastZ;
            cameraEntity.setYaw(yaw);
            cameraEntity.setPitch(pitch);
            cameraEntity.lastYaw = lastYaw;
            cameraEntity.lastPitch = lastPitch;
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(float tickProgress, boolean sleeping, Matrix4f positionMatrix, CallbackInfo ci) {
        if ((zoom.isEnabled() && !zoom.showHands.getValue())
                || (freecam.isEnabled() && !freecam.renderHands.getValue()))
            ci.cancel();
    }

    @ModifyVariable(method = "renderWorld", ordinal = 6, at = @At(value = "STORE"))
    private float noRenderNauseaDistortion(float scaledNauseaEffectFactor) {
        return (noRender.isEnabled() && !noRender.nauseaEffect.getValue()) ? 0 : scaledNauseaEffectFactor;
    }
}
