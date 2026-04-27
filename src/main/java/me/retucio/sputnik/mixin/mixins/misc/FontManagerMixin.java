package me.retucio.sputnik.mixin.mixins.misc;

import me.retucio.sputnik.Sputnik;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.Fonts;

import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(FontManager.class)
public abstract class FontManagerMixin {

    @Shadow
    protected abstract FontSet getFontSetRaw(Identifier id);

    @Inject(method = "getFontSetRaw", at = @At("HEAD"), cancellable = true)
    private void onGetStorageInternal(Identifier id, CallbackInfoReturnable<FontSet> cir) {
        Fonts fonts = ModuleManager.INSTANCE.getModuleByClass(Fonts.class);
        if (!fonts.isEnabled()) return;

        if (id.equals(FontDescription.Resource.DEFAULT.id())) {
            cir.setReturnValue(getFontSetRaw(
                    Identifier.fromNamespaceAndPath(
                        Sputnik.MOD_ID,
                        fonts.getFont()
                    )
            ));
        }
    }
}