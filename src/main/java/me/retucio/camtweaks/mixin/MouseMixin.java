package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.MouseClickEvent;
import me.retucio.camtweaks.event.events.MouseScrollEvent;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        MouseClickEvent event = EVENT_BUS.post(new MouseClickEvent(action, input.button()));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrollEvent event = EVENT_BUS.post(new MouseScrollEvent(horizontal, vertical));
        if (event.isCancelled()) ci.cancel();
    }
}
