package me.retucio.sputnik.mixin.mixins.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Unique
    private static NoRender noRender;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void noRenderFireOverlay(PoseStack matrices, MultiBufferSource bufferSource, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.fireOverlay.getValue()) ci.cancel();
    }

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void noRenderUnderwaterOverlay(Minecraft minecraft, PoseStack matrices, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.fluidOverlay.getValue()) ci.cancel();
    }
}