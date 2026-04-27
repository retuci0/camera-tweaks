package me.retucio.sputnik.mixin.mixins.io;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.input.MouseClickEvent;
import me.retucio.sputnik.event.input.MouseScrollEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long handle, MouseButtonInfo input, int action, CallbackInfo ci) {
        Sputnik.INSTANCE.onInput(input.button(), action);
        MouseClickEvent event = Sputnik.EVENT_BUS.post(new MouseClickEvent(action, input.button()));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseScrollEvent event = Sputnik.EVENT_BUS.post(new MouseScrollEvent(horizontal, vertical));
        if (event.isCancelled()) ci.cancel();
    }
}
