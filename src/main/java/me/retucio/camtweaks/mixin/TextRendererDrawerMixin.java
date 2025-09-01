package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.NoRender;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// falta italics pero no sé dónde se comprueba eso, no es aquí :P
@Mixin(TextRenderer.Drawer.class)
public abstract class TextRendererDrawerMixin {

    @Unique
    NoRender noRender;

    @Inject(method = "<init>(Lnet/minecraft/client/font/TextRenderer;FFIIZ)V", at = @At("TAIL"))
    private void getModules(TextRenderer textRenderer, float x, float y, int color, int backgroundColor, boolean shadow, CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isBold()Z"))
    private boolean noRenderBold(Style style) {
        return !(noRender.isEnabled() && !noRender.bold.isEnabled()) && style.isBold();
    }

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isUnderlined()Z"))
    private boolean noRenderUnderlined(Style style) {
        return !(noRender.isEnabled() && !noRender.underlined.isEnabled()) && style.isUnderlined();
    }

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isStrikethrough()Z"))
    private boolean noRenderStrikethrough(Style style) {
        return !(noRender.isEnabled() && !noRender.strikethrough.isEnabled()) && style.isStrikethrough();
    }

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isObfuscated()Z"))
    private boolean noRenderMTS(Style style) {
        return !(noRender.isEnabled() && !noRender.scrambledText.isEnabled()) && style.isObfuscated();
    }

    @ModifyArg(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer$Drawer;getRenderColor(Lnet/minecraft/text/TextColor;)I"))
    private TextColor noRenderColor(TextColor original) {
        return !noRender.color.is(NoRender.Colors.DEFAULT) ? TextColor.fromFormatting(noRender.color.getValue().toFormatting()) : original;
    }
}
