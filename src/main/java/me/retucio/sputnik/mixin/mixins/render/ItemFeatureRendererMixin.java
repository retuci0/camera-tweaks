package me.retucio.sputnik.mixin.mixins.render;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.GlintPlus;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFeatureRenderer.class)
public abstract class ItemFeatureRendererMixin {

    // obtener módulo en cada redirect, ya que tiene un inicializador estático

    @Shadow
    private static boolean useTransparentGlint(RenderType renderType) {
        throw new UnsupportedOperationException("implemented via mixin");
    }

    @Redirect(method = "getFoilRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;useTransparentGlint(Lnet/minecraft/client/renderer/rendertype/RenderType;)Z"))
    private static boolean alwaysUseTranslucentGlint(RenderType renderType) {
        GlintPlus glintPlus = ModuleManager.INSTANCE.getModuleByClass(GlintPlus.class);
        if (!glintPlus.isEnabled()) {
            useTransparentGlint(renderType);
        }
        return true;
    }

    @Redirect(method = "getFoilRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;glint()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static RenderType redirectGlint() {
        GlintPlus glintPlus = ModuleManager.INSTANCE.getModuleByClass(GlintPlus.class);
        if (glintPlus == null) return RenderTypes.glint();
        return glintPlus.getGlintTranslucent();
    }

    @Redirect(method = "getFoilRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entityGlint()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static RenderType redirectEntityGlint() {
        GlintPlus glintPlus = ModuleManager.INSTANCE.getModuleByClass(GlintPlus.class);
        if (glintPlus == null) return RenderTypes.entityGlint();
        return glintPlus.getGlintTranslucent();
    }

    @Redirect(method = "getFoilRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;glintTranslucent()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static RenderType redirectGlintTranslucent() {
        GlintPlus glintPlus = ModuleManager.INSTANCE.getModuleByClass(GlintPlus.class);
        if (glintPlus == null) return RenderTypes.glintTranslucent();
        return glintPlus.getGlintTranslucent();
    }
}
