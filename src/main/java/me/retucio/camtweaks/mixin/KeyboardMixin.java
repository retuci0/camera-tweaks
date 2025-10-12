package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.KeyEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKeyPress(long window, int action, KeyInput input, CallbackInfo ci) {
        CameraTweaks.INSTANCE.onKeyPress(input.key(), action);

        KeyEvent event = EVENT_BUS.post(new KeyEvent(input.key(), input.scancode(), action));
        if (event.isCancelled()) ci.cancel();
    }
}
