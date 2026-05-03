package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.render.GetFOVEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freelook;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.camera.PerspectivePlus;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;


@Mixin(Camera.class)
public abstract class CameraMixin {

    @Unique
    private float tickDelta;

    @Unique
    PerspectivePlus perspectivePlus;

    @Unique
    Freecam freecam;

    @Unique
    Freelook freelook;

    @Shadow
    private boolean detached;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        perspectivePlus = ModuleManager.INSTANCE.getModuleByClass(PerspectivePlus.class);
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        freelook = ModuleManager.INSTANCE.getModuleByClass(Freelook.class);
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdateHead(DeltaTracker deltaTracker, CallbackInfo ci) {
        this.tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
    }

    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float modifyFov(float original) {
        return Sputnik.EVENT_BUS.post(new GetFOVEvent(original)).getFov();
    }

    // perspectiva

    @ModifyVariable(method = "getMaxZoom", at = @At("HEAD"), argsOnly = true, name = "cameraDist")
    private float modifyClipToSpace(float distance) {
        if (freecam.isEnabled()) return 0;
        return perspectivePlus.isEnabled() ? (float) perspectivePlus.getDistance() : distance;
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float distance, CallbackInfoReturnable<Float> cir) {
        if (perspectivePlus.isEnabled() && perspectivePlus.clip.getValue())
            cir.setReturnValue(distance);
    }


    // cámara libre

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdateTail(final DeltaTracker dt, CallbackInfo ci) {
        if (freecam.isEnabled()) this.detached = true;
    }

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void onUpdateSetPosArgs(Args args) {
        if (freecam.isEnabled()) {
            args.set(0, freecam.getX(tickDelta));
            args.set(1, freecam.getY(tickDelta));
            args.set(2, freecam.getZ(tickDelta));
        }
    }

    @ModifyArgs(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        if (freecam.isEnabled()) {
            args.set(0, freecam.getYaw(tickDelta));
            args.set(1, freecam.getPitch(tickDelta));
        } else if (freelook.isEnabled()) {
            args.set(0, freelook.getYaw());
            args.set(1, freelook.getPitch());
        }
    }
}