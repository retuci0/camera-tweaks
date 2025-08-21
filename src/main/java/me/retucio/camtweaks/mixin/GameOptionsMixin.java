package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.PerspectiveChangeEvent;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void changePerspective(Perspective perspective, CallbackInfo ci) {
        PerspectiveChangeEvent event = EVENT_BUS.post(new PerspectiveChangeEvent(perspective));
        if (event.isCancelled()) ci.cancel();
    }
}
