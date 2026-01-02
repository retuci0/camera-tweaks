package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.camera.Freelook;
import me.retucio.camtweaks.module.modules.camera.Freecam;
import me.retucio.camtweaks.module.modules.camera.PerspectivePlus;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
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
    private boolean thirdPerson;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        perspectivePlus = ModuleManager.INSTANCE.getModuleByClass(PerspectivePlus.class);
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        freelook = ModuleManager.INSTANCE.getModuleByClass(Freelook.class);
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdateHead(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        this.tickDelta = tickDelta;
    }


    // perspectiva

    @ModifyVariable(method = "clipToSpace", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyClipToSpace(float distance) {
        if (freecam.isEnabled()) return 0;
        return perspectivePlus.isEnabled() ? (float) perspectivePlus.getDistance() : distance;
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float distance, CallbackInfoReturnable<Float> cir) {
        if (perspectivePlus.isEnabled() && perspectivePlus.clip.isEnabled())
            cir.setReturnValue(distance);
    }


    // cámara libre

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdateTail(World area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        if (freecam.isEnabled())
            this.thirdPerson = true;
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void onUpdateSetPosArgs(Args args) {
        if (freecam.isEnabled()) {
            args.set(0, freecam.getX(tickDelta));
            args.set(1, freecam.getY(tickDelta));
            args.set(2, freecam.getZ(tickDelta));
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
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