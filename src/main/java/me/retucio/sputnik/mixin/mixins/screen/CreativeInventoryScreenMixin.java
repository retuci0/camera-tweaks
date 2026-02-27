package me.retucio.sputnik.mixin.mixins.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.inventory.CreativeInventoryHotbarKeybinds;
import me.retucio.sputnik.util.KeyUtil;
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
            return Arrays.stream(mc.options.hotbarKeys).anyMatch(keyBinding -> KeyUtil.getKey(keyBinding) == input.getKeycode());
        return original;
    }
}
