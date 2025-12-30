package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.ChatPlus;
import me.retucio.camtweaks.module.modules.Freecam;
import me.retucio.camtweaks.module.modules.NoRender;
import me.retucio.camtweaks.ui.hud.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Unique
    NoRender noRender;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(MinecraftClient client, CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }


    // norender

    @Inject(method = "renderScoreboardSidebar*", at = @At("HEAD"), cancellable = true)
    private void noRenderScoreboard(CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.scoreboard.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderTitleAndSubtitle", at = @At("HEAD"), cancellable = true)
    private void noRenderTitles(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.titles.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void noRenderNausea(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.nauseaEffect.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void noRenderSpyglass(DrawContext context, float scale, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.spyglassOverlay.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void noRenderPortal(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.portalOverlay.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void noRenderPumpkinBlur(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.pumpkinOverlay.isEnabled() && texture.getPath().equals("textures/misc/pumpkinblur.png")) ci.cancel();
    }


    // otros

    @Inject(method = "render", at = @At("RETURN"))
    private void renderHUD(DrawContext ctx, RenderTickCounter tc, CallbackInfo ci) {
        HudRenderer.render(ctx, tc);
    }

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"))
    private boolean alwaysRenderCrosshairInFreecam(boolean firstPerson) {
        return ModuleManager.INSTANCE.getModuleByClass(Freecam.class).isEnabled() || firstPerson;
    }

    @Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
    private void onClear(CallbackInfo ci) {
        ChatPlus chatPlus = ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class);
        if (chatPlus.isEnabled() && chatPlus.keepHistory.isEnabled()) ci.cancel();
    }
}
