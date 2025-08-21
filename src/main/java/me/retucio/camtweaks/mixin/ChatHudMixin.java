package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.camtweaks.event.events.ReceiveMessageEvent;
import me.retucio.camtweaks.module.modules.BetterChat;
import me.retucio.camtweaks.util.interfaces.IChatHud;
import me.retucio.camtweaks.util.interfaces.IChatHudLine;
import me.retucio.camtweaks.util.interfaces.IChatHudLineVisible;
import me.retucio.camtweaks.util.interfaces.IMessageHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static me.retucio.camtweaks.CameraTweaks.*;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin implements IChatHud {

    @Unique
    private boolean skipOnAddMessage;

    @Unique
    private int nextId;

    @Unique @Final
    private BetterChat betterChat = moduleManager.getModuleByClass(BetterChat.class);

    @Shadow @Final
    private List<ChatHudLine> messages;

    @Shadow @Final
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    public abstract void addMessage(Text message);

    @Shadow
    public abstract void addMessage(Text message, @Nullable MessageSignatureData signatureData, @Nullable MessageIndicator indicator);



    // métodos relacionados a las interfaces IChatHud, IChatHudLine, IChadHudLineVisible y IMessageHandler

    @Override
    public void smegma$add(Text message, int id) {
        nextId = id;
        addMessage(message);
        nextId = 0;
    }

    @Inject(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLineVisible(ChatHudLine message, CallbackInfo ci) {
        ((IChatHudLine) (Object) visibleMessages.getFirst()).smegma$setId(nextId);
    }

    @Inject(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLine(ChatHudLine message, CallbackInfo ci) {
        ((IChatHudLine) (Object) messages.getFirst()).smegma$setId(nextId);
    }

    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(method = "addVisibleMessage", at = @At(value = "NEW", target = "(ILnet/minecraft/text/OrderedText;Lnet/minecraft/client/gui/hud/MessageIndicator;Z)Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;"))
    private ChatHudLine.Visible onAddMessage_modifyChatHudLineVisible(ChatHudLine.Visible line, @Local(ordinal = 1) int j) {
        IMessageHandler handler = (IMessageHandler) mc.getMessageHandler();
        if (handler == null) return line;

        IChatHudLineVisible iLine = (IChatHudLineVisible) (Object) line;

        iLine.smegma$setSender(handler.smegma$getSender());
        iLine.smegma$setStartOfEntry(j == 0);

        return line;
    }

    @ModifyExpressionValue(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "NEW", target = "(ILnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)Lnet/minecraft/client/gui/hud/ChatHudLine;"))
    private ChatHudLine onAddMessage_modifyChatHudLine(ChatHudLine line) {
        IMessageHandler handler = (IMessageHandler) mc.getMessageHandler();
        if (handler == null) return line;

        ((IChatHudLine) (Object) line).smegma$setSender(handler.smegma$getSender());
        return line;
    }



    // modificar contenido del mensaje
    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", cancellable = true)
    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        if (skipOnAddMessage) return;

        ReceiveMessageEvent event = EVENT_BUS.post(new ReceiveMessageEvent(message, indicator, nextId));

        if (event.isCancelled()) {
            ci.cancel();
        } else {
            visibleMessages.removeIf(msg -> ((IChatHudLine) (Object) msg).smegma$getId() == nextId && nextId != 0);

            for (int i = messages.size() - 1; i > -1; i--) {
                if (((IChatHudLine) (Object) messages.get(i)).smegma$getId() == nextId && nextId != 0) {
                    messages.remove(i);
                    betterChat.removeLine(i);
                }
            }

            if (event.wasModified()) {
                ci.cancel();

                skipOnAddMessage = true;
                addMessage(event.getMessage(), signatureData, event.getIndicator());
                skipOnAddMessage = false;
            }
        }
    }



    // cabezas

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I"))
    private int onRender_modifyWidth(int width) {
        return (betterChat.isEnabled() && betterChat.showHeads.isEnabled()) ? width + 10 : width;
    }

    @ModifyReceiver(method = "method_71991", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)V"))
    private DrawContext onRender_beforeDrawTextWithShadow(DrawContext context, TextRenderer textRenderer, OrderedText text, int x, int y, int color, @Local(argsOnly = true) ChatHudLine.Visible line) {
        betterChat.beforeDrawMessage(context, line, y, color);
        return context;
    }

    @Inject(method = "method_71991", at = @At("TAIL"))
    private void onRender_afterDrawTextWithShadow(int i, DrawContext context, float f, int j, int k, int l, ChatHudLine.Visible visible, int m, float g, CallbackInfo info) {
        betterChat.afterDrawMessage(context);
    }



    // historial (registro) del chat

    @ModifyExpressionValue(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int maxLength(int size) {
        if (!betterChat.isEnabled()) return size;
        return size + betterChat.chatHistoryExtraLength.getIntValue();
    }

    @ModifyExpressionValue(method = "addVisibleMessage", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int maxLengthVisible(int size) {
        if (!betterChat.isEnabled()) return size;
        return size + betterChat.chatHistoryExtraLength.getIntValue();
    }

    @Inject(method = "addVisibleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"))
    private void onBreakChatMessageLines(ChatHudLine message, CallbackInfo ci, @Local List<OrderedText> list) {
        betterChat.lines.addFirst(list.size());
    }

    @Inject(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(I)Ljava/lang/Object;"))
    private void onRemoveMessage(ChatHudLine message, CallbackInfo ci) {
        int extra = betterChat.chatHistoryExtraLength.getIntValue();
        int size = betterChat.lines.size();

        while (size > 100 + extra) {
            betterChat.lines.removeLast();
            size--;
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void onClear(boolean clearHistory, CallbackInfo ci) {
        betterChat.lines.clear();
    }

    @Inject(method = "refresh", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci) {
        betterChat.lines.clear();
    }

}