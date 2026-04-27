package me.retucio.sputnik.mixin.mixins.screen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.inventory.CreativeInventoryHotbarKeybinds;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;


@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {

    @ModifyExpressionValue(method = "keyPressed", at = @At(value = "INVOKE", target = "Ljava/util/OptionalInt;isPresent()Z"))
    private boolean makeHotkeysWork(boolean original, @Local(argsOnly = true, name = "event") KeyEvent event) {
        if (ModuleManager.INSTANCE.getModuleByClass(CreativeInventoryHotbarKeybinds.class).isEnabled())
            return Arrays.stream(Sputnik.mc.options.keyHotbarSlots).anyMatch(keyBinding -> KeyUtil.getKey(keyBinding) == event.key());
        return original;
    }
}
