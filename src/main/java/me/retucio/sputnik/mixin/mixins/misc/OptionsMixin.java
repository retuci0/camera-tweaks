package me.retucio.sputnik.mixin.mixins.misc;

import me.retucio.sputnik.event.render.PerspectiveChangeEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;


@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow
    public abstract CameraType getCameraType();

    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void changePerspective(CameraType perspective, CallbackInfo ci) {
        if (getCameraType() == null) return;
        if (ModuleManager.INSTANCE.getModuleByClass(Freecam.class).isEnabled()) ci.cancel();
        PerspectiveChangeEvent event = EVENT_BUS.post(new PerspectiveChangeEvent(perspective));
        if (event.isCancelled()) ci.cancel();
    }
}
