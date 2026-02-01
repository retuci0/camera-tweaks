package me.retucio.sputnik.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.mixin.accessor.KeyBindingAccessor;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.CreativeInventoryHotbarKeybinds;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

import static me.retucio.sputnik.Sputnik.mc;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @ModifyExpressionValue(method = "keyPressed", at = @At(value = "INVOKE", target = "Ljava/util/OptionalInt;isPresent()Z"))
    private boolean makeHotkeysWork(boolean original, @Local(argsOnly = true) KeyInput input) {
        if (ModuleManager.INSTANCE.getModuleByClass(CreativeInventoryHotbarKeybinds.class).isEnabled())
            return Arrays.stream(mc.options.hotbarKeys).anyMatch(
                    keyBinding -> ((KeyBindingAccessor) keyBinding).getBoundKey().getCode() == input.getKeycode()
            );
        return original;
    }
}
