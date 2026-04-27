package me.retucio.sputnik.mixin.mixins.hud;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.network.ReceiveMessageEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ChatPlus;

import me.retucio.sputnik.util.interfaces.IChatComponent;
import me.retucio.sputnik.util.interfaces.IGuiMessage;
import me.retucio.sputnik.util.interfaces.IGuiMessageLine;
import me.retucio.sputnik.util.interfaces.IChatListener;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements IChatComponent {

    @Unique
    private boolean skipOnAddMessage;

    @Unique
    private int nextId;

    @Unique
    ChatPlus chatPlus;

    @Shadow @Final
    private List<GuiMessage> allMessages;

    @Shadow @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Shadow
    public abstract void addClientSystemMessage(Component message);

    @Shadow
    protected abstract void addMessage(final Component contents, final @Nullable MessageSignature signature, final GuiMessageSource source, final @Nullable GuiMessageTag tag);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        chatPlus = ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class);
    }


    // métodos relacionados con las interfaces IChatHud, IChatHudLine, IChadHudLineVisible y IMessageHandler

    @Override
    public void sputnik$add(Component message, int id) {
        nextId = id;
        addClientSystemMessage(message);
        nextId = 0;
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLineVisible(GuiMessage message, CallbackInfo ci) {
        ((IGuiMessage) (Object) trimmedMessages.getFirst()).sputnik$setId(nextId);
    }

    @Inject(method = "addMessageToQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLine(GuiMessage message, CallbackInfo ci) {
        ((IGuiMessage) (Object) allMessages.getFirst()).sputnik$setId(nextId);
    }

    @ModifyExpressionValue(method = "addMessageToDisplayQueue", at = @At(value = "NEW", target = "(Lnet/minecraft/client/multiplayer/chat/GuiMessage;Lnet/minecraft/util/FormattedCharSequence;Z)Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;"))
    private GuiMessage.Line onAddMessage_modifyChatHudLineVisible(GuiMessage.Line line, @Local(name = "i") int i) {
        IChatListener iListener = (IChatListener) Sputnik.mc.getChatListener();
        IGuiMessageLine iLine = (IGuiMessageLine) (Object) line;

        iLine.sputnik$setSender(iListener.sputnik$getSender());
        iLine.sputnik$setStartOfEntry(i == 0);

        return line;
    }

    @ModifyExpressionValue(method = "addMessage", at = @At(value = "NEW", target = "(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)Lnet/minecraft/client/multiplayer/chat/GuiMessage;"))
    private GuiMessage onAddMessage_modifyChatHudLine(GuiMessage line) {
        IChatListener iListener = (IChatListener) Sputnik.mc.getChatListener();
        ((IGuiMessage) (Object) line).sputnik$setSender(iListener.sputnik$getSender());
        return line;
    }

    @Override
    public List<GuiMessage.Line> sputnik$getVisibleMessages() {
        return trimmedMessages;
    }


    // modificar contenido del mensaje

    @Inject(at = @At("HEAD"), method = "addMessage", cancellable = true)
    private void onAddMessage(Component contents, MessageSignature signature, GuiMessageSource source, GuiMessageTag tag, CallbackInfo ci) {
        if (skipOnAddMessage) return;

        ReceiveMessageEvent event = Sputnik.EVENT_BUS.post(new ReceiveMessageEvent(contents, tag, nextId));

        if (event.isCancelled()) {
            ci.cancel();
        } else {
            trimmedMessages.removeIf(msg -> ((IGuiMessage) (Object) msg).sputnik$getId() == nextId && nextId != 0);

            for (int i = allMessages.size() - 1; i > -1; i--) {
                if (((IGuiMessage) (Object) allMessages.get(i)).sputnik$getId() == nextId && nextId != 0) {
                    allMessages.remove(i);
                    chatPlus.removeLine(i);
                }
            }

            if (event.wasModified()) {
                ci.cancel();

                skipOnAddMessage = true;
                addMessage(event.getMessage(), signature, source, event.getTag());
                skipOnAddMessage = false;
            }
        }
    }



    // cabezas

    @ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;ceil(F)I"))
    private int onRender_modifyWidth(int width) {
        return (chatPlus.isEnabled() && chatPlus.showHeads.getValue()) ? width + 10 : width;
    }


    // historial (registro) del chat

    @ModifyExpressionValue(method = "addMessageToQueue", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int maxLength(int size) {
        if (!chatPlus.isEnabled()) return size;
        return size + chatPlus.chatHistoryExtraLength.getIntValue();
    }

    @ModifyExpressionValue(method = "addMessageToDisplayQueue", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int maxLengthVisible(int size) {
        if (!chatPlus.isEnabled()) return size;
        return size + chatPlus.chatHistoryExtraLength.getIntValue();
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;isChatFocused()Z"))
    private void onBreakChatMessageLines(GuiMessage message, CallbackInfo ci, @Local(name = "lines") List<FormattedCharSequence> lines) {
        chatPlus.lines.addFirst(lines.size());
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;removeLast()Ljava/lang/Object;"))
    private void onRemoveMessage(GuiMessage message, CallbackInfo ci) {
        int extra = chatPlus.chatHistoryExtraLength.getIntValue();
        int size = chatPlus.lines.size();

        while (size > 100 + extra) {
            chatPlus.lines.removeLast();
            size--;
        }
    }

    @Inject(method = "clearMessages", at = @At("HEAD"))
    private void onClear(boolean history, CallbackInfo ci) {
        chatPlus.lines.clear();
    }

    @Inject(method = "refreshTrimmedMessages", at = @At("HEAD"))
    private void onRefresh(CallbackInfo ci) {
        chatPlus.lines.clear();
    }
}