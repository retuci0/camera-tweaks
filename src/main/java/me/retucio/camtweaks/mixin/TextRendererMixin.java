package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.NoRender;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

    @ModifyExpressionValue(method = "getGlyph", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isObfuscated()Z"))
    private boolean noRenderMTS(boolean original) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        return (!noRender.isEnabled() || noRender.scrambledText.isEnabled()) && original;
    }
}
