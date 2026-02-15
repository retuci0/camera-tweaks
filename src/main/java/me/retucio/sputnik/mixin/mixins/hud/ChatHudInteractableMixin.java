package me.retucio.sputnik.mixin.mixins.hud;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ChatPlus;
import net.minecraft.client.font.Alignment;
import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHud.Interactable.class)
public abstract class ChatHudInteractableMixin {

    @Unique
    private static ChatPlus chatPlus;

    @Shadow @Final
    private DrawContext context;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, boolean bl, CallbackInfo ci) {
        chatPlus = ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class);
    }

    @ModifyReceiver(method = "text", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/DrawnTextConsumer;text(Lnet/minecraft/client/font/Alignment;IILnet/minecraft/client/font/DrawnTextConsumer$Transformation;Lnet/minecraft/text/OrderedText;)V"))
    private DrawnTextConsumer onRender_beforeDrawTextWithShadow(DrawnTextConsumer instance, Alignment alignment, int x, int y, DrawnTextConsumer.Transformation transformation, OrderedText orderedText) {
        chatPlus.beforeDrawMessage(context, y, ColorHelper.getWhite(transformation.opacity()));
        return instance;
    }

    @ModifyArg(method = "text", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/DrawnTextConsumer;text(Lnet/minecraft/client/font/Alignment;IILnet/minecraft/client/font/DrawnTextConsumer$Transformation;Lnet/minecraft/text/OrderedText;)V"), index = 1)
    private int modifyX(int x) {
        return (chatPlus.isEnabled() && chatPlus.showHeads.getValue()) ? x + 10 : x;
    }

    @Inject(method = "text", at = @At("TAIL"))
    private void onRender_afterDrawTextWithShadow(int y, float f, OrderedText text, CallbackInfoReturnable<Boolean> cir) {
        chatPlus.afterDrawMessage();
    }
}
