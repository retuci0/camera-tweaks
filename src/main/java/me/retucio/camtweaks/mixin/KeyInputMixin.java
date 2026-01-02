package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.camera.Freecam;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyInputMixin extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void isPressed(CallbackInfo ci) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        if (freecam.isEnabled() && freecam.stayCrouching.isEnabled() && freecam.isCrouching())
            playerInput = new PlayerInput(
                false, false,
                false, false,
                false,
                true,  // siempre agachado
                false
        );
    }
}