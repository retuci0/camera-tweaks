package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.render.GlintPlus;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Unique
    private static GlintPlus glintPlus;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        glintPlus = ModuleManager.INSTANCE.getModuleByClass(GlintPlus.class);
    }

    @Redirect(method = "getItemGlintConsumer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayers;glint()Lnet/minecraft/client/render/RenderLayer;"))
    private static RenderLayer getGlint() {
        return glintPlus.getGlint();
    }

    @Redirect(method = "getItemGlintConsumer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayers;entityGlint()Lnet/minecraft/client/render/RenderLayer;"))
    private static RenderLayer getEntityGlint() {
        return glintPlus.getEntityGlint();
    }
}
