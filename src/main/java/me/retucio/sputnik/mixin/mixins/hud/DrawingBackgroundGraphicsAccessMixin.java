package me.retucio.sputnik.mixin.mixins.hud;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ChatPlus;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * al parecer si hago un mixin combinado ninguno de los dos funciona
 * @see DrawingFocusedGraphicsAccessMixin
 */

@Mixin(ChatComponent.DrawingBackgroundGraphicsAccess.class)
public abstract class DrawingBackgroundGraphicsAccessMixin {

    @Unique
    private static ChatPlus chatPlus;

    @Shadow @Final
    private GuiGraphicsExtractor graphics;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        chatPlus = ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class);
    }

    @ModifyReceiver(method = "handleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V"))
    private ActiveTextCollector onRender_beforeDrawTextWithShadow(ActiveTextCollector instance, TextAlignment textAlignment, int x, int y, ActiveTextCollector.Parameters parameters, FormattedCharSequence formattedCharSequence) {
        chatPlus.beforeDrawMessage(graphics, y, ARGB.white(parameters.opacity()));
        return instance;
    }

    @ModifyArg(method = "handleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V"), index = 1)
    private int modifyX(int x) {
        return (chatPlus.isEnabled() && chatPlus.showHeads.getValue()) ? x + 10 : x;
    }

    @Inject(method = "handleMessage", at = @At("TAIL"))
    private void onRender_afterDrawTextWithShadow(int textTop, float opacity, FormattedCharSequence message, CallbackInfoReturnable<Boolean> cir) {
        chatPlus.afterDrawMessage();
    }
}
