package me.retucio.sputnik.mixin.mixins.io;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.input.KeyEvent;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long handle, int action, net.minecraft.client.input.KeyEvent input, CallbackInfo ci) {
        if (input.key() == -1) return;
        Sputnik.INSTANCE.onInput(input.key(), action);
        KeyEvent event = Sputnik.EVENT_BUS.post(new KeyEvent(input.key(), input.scancode(), action));
        if (event.isCancelled()) ci.cancel();
    }
}
