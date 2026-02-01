package me.retucio.sputnik.mixin;

import me.retucio.sputnik.Sputnik;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.Fonts;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(FontManager.class)
public abstract class FontManagerMixin {

    @Shadow
    public abstract FontStorage getStorageInternal(Identifier id);

    @Inject(method = "getStorageInternal", at = @At("HEAD"), cancellable = true)
    private void onGetStorageInternal(Identifier id, CallbackInfoReturnable<FontStorage> cir) {
        Fonts fonts = ModuleManager.INSTANCE.getModuleByClass(Fonts.class);
        if (!fonts.isEnabled()) return;

        if (id.equals(StyleSpriteSource.Font.DEFAULT.id())) {
            cir.setReturnValue(getStorageInternal(
                    Identifier.of(
                        Sputnik.MOD_ID,
                        fonts.getFont()
                    )
            ));
        }
    }
}
