package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.event.events.GetFOVEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Freecam;
import me.retucio.camtweaks.module.modules.PerspectivePlus;
import me.retucio.camtweaks.module.modules.Zoom;
import me.retucio.camtweaks.util.interfaces.IVec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique
    boolean freecamDone;

    @Unique
    Zoom zoom;

    @Unique
    Freecam freecam;

    @Shadow @Final
    private MinecraftClient client;

    @Shadow
    public abstract void updateCrosshairTarget(float tickDelta);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(MinecraftClient client, HeldItemRenderer firstPersonHeldItemRenderer, BufferBuilderStorage buffers, CallbackInfo ci) {
        zoom = ModuleManager.INSTANCE.getModuleByClass(Zoom.class);;
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
    }

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float modifyFov(float original) {
        return EVENT_BUS.post(new GetFOVEvent(original)).getFov();
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

            ((IVec3d) cameraEntity.getPos()).smegma$set(pos.x, pos.y - cameraEntity.getEyeHeight(cameraEntity.getPose()), pos.z);
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

            ((IVec3d) cameraEntity.getPos()).smegma$set(x, y, z);
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
        if ((zoom.isEnabled() && !zoom.showHands.isEnabled())
                || (freecam.isEnabled() && !freecam.renderHands.isEnabled()))
            ci.cancel();
    }
}
