package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.modules.BetterChat;
import me.retucio.camtweaks.ui.HUD;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.moduleManager;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"), cancellable = true)
    private void renderHUD(DrawContext ctx, RenderTickCounter tc, CallbackInfo ci) {
        HUD.render(ctx, tc);
    }

    @Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
    private void onClear(CallbackInfo ci) {
        BetterChat betterChat = moduleManager.getModuleByClass(BetterChat.class);
        if (betterChat.isEnabled() && betterChat.logger.isEnabled()) ci.cancel();
    }
}
