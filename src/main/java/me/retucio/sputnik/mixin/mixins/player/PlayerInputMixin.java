package me.retucio.sputnik.mixin.mixins.player;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.input.SneakEvent;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInput.class)
public abstract class PlayerInputMixin {

    @Inject(method = "sneak", at = @At("HEAD"), cancellable = true)
    private void onSneak(CallbackInfoReturnable<Boolean> cir) {
        SneakEvent event = Sputnik.EVENT_BUS.post(new SneakEvent());
        if (event.isCancelled()) cir.cancel();
    }
}
