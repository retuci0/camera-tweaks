package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.NoRender;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(Font.class)
public abstract class FontMixin {

    @ModifyExpressionValue(method = "getGlyph", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;isObfuscated()Z"))
    private boolean noRenderMTS(boolean original) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        return (!noRender.isEnabled() || noRender.scrambledText.getValue()) && original;
    }
}
