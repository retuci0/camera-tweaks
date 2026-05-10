package me.retucio.sputnik.mixin.mixins.hud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.render.Render2DEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.CrosshairPlus;
import me.retucio.sputnik.module.modules.misc.ChatPlus;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.render.NoRender;
import me.retucio.sputnik.ui.hud.HudRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Unique
    NoRender noRender;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRenderHud(GuiGraphicsExtractor gui, DeltaTracker dt, CallbackInfo ci) {
        Sputnik.EVENT_BUS.post(new Render2DEvent(gui, dt));
    }


    // norender

    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void noRenderScoreboard(CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.scoreboard.getValue()) ci.cancel();
    }

    @Inject(method = "extractTitle", at = @At("HEAD"), cancellable = true)
    private void noRenderTitles(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.titles.getValue()) ci.cancel();
    }

    @Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void noRenderNausea(GuiGraphicsExtractor graphics, float strength, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.nauseaEffect.getValue()) ci.cancel();
    }

    @Inject(method = "extractSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void noRenderSpyglass(GuiGraphicsExtractor graphics, float scale, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.spyglassOverlay.getValue()) ci.cancel();
    }

    @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void noRenderPortal(GuiGraphicsExtractor graphics, float alpha, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.portalOverlay.getValue()) ci.cancel();
    }

    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void noRenderPumpkinBlur(GuiGraphicsExtractor graphics, Identifier texture, float alpha, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.pumpkinOverlay.getValue() && texture.getPath().equals("textures/misc/pumpkinblur.png")) ci.cancel();
    }


    // otros

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void renderHUD(GuiGraphicsExtractor gui, DeltaTracker dt, CallbackInfo ci) {
        HudRenderer.INSTANCE.render(gui, dt);
    }

    @Redirect(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void onRenderCrosshair(GuiGraphicsExtractor gui, RenderPipeline pipeline, Identifier id, int x, int y, int width, int height) {
        // dangerous abbreviation
        CrosshairPlus cp = ModuleManager.INSTANCE.getModuleByClass(CrosshairPlus.class);

        int color = cp.isEnabled() ? cp.color.getRGB() : -1;
        int w = cp.isEnabled() ? cp.width.getIntValue() : width;
        int h = cp.isEnabled() ? cp.height.getIntValue() : height;

        gui.blitSprite(pipeline, id, (gui.guiWidth() - w) / 2, (gui.guiHeight() - h) / 2, w, h, color);
    }

    @ModifyExpressionValue(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean alwaysRenderCrosshairInFreecam(boolean firstPerson) {
        return ModuleManager.INSTANCE.getModuleByClass(Freecam.class).isEnabled() || firstPerson;
    }

    @Inject(method = "onDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;clearMessages(Z)V"), cancellable = true)
    private void onClear(CallbackInfo ci) {
        ChatPlus chatPlus = ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class);
        if (chatPlus.isEnabled() && chatPlus.keepHistory.getValue()) ci.cancel();
    }
}
