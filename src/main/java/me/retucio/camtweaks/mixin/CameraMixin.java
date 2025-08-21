package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.module.modules.Perspective;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.camtweaks.CameraTweaks.moduleManager;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyVariable(method = "clipToSpace", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyClipToSpace(float distance) {
        Perspective perspective = moduleManager.getModuleByClass(Perspective.class);
        return perspective.isEnabled() ? (float) perspective.getDistance() : distance;
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float distance, CallbackInfoReturnable<Float> cir) {
        if (moduleManager.getModuleByClass(Perspective.class).isEnabled()
            && moduleManager.getModuleByClass(Perspective.class).clip.isEnabled())
            cir.setReturnValue(distance);
    }
}
