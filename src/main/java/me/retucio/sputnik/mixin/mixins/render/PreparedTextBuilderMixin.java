package me.retucio.sputnik.mixin.mixins.render;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.NoRender;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// falta italics pero no sé dónde se comprueba eso, no es aquí :P
@Mixin(Font.PreparedTextBuilder.class)
public abstract class PreparedTextBuilderMixin {

    @Unique
    NoRender noRender;

    @Inject(method = "<init>(Lnet/minecraft/client/gui/Font;FFIIZZ)V", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Redirect(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;isBold()Z"))
    private boolean noRenderBold(Style style) {
        return !(noRender.isEnabled() && !noRender.bold.getValue()) && style.isBold();
    }

    @Redirect(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;isUnderlined()Z"))
    private boolean noRenderUnderlined(Style style) {
        return !(noRender.isEnabled() && !noRender.underlined.getValue()) && style.isUnderlined();
    }

    @Redirect(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;isStrikethrough()Z"))
    private boolean noRenderStrikethrough(Style style) {
        return !(noRender.isEnabled() && !noRender.strikethrough.getValue()) && style.isStrikethrough();
    }

    @ModifyArg(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font$PreparedTextBuilder;getTextColor(Lnet/minecraft/network/chat/TextColor;)I"))
    private @Nullable TextColor noRenderColor(@Nullable TextColor original) {
        return !noRender.color.is(NoRender.Colors.DEFAULT) ? TextColor.fromLegacyFormat(noRender.color.getValue().toFormatting()) : original;
    }
}
