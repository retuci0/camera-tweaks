package me.retucio.sputnik.mixin;

import me.retucio.sputnik.event.events.render.PerspectiveChangeEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

    @Shadow
    public abstract Perspective getPerspective();

    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void changePerspective(Perspective perspective, CallbackInfo ci) {
        if (getPerspective() == null) return;
        if (ModuleManager.INSTANCE.getModuleByClass(Freecam.class).isEnabled()) ci.cancel();
        PerspectiveChangeEvent event = EVENT_BUS.post(new PerspectiveChangeEvent(perspective));
        if (event.isCancelled()) ci.cancel();
    }
}
