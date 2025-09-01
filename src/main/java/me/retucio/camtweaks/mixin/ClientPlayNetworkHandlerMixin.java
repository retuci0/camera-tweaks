package me.retucio.camtweaks.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.event.events.SendMessageEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.NoRender;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.*;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Unique
    private boolean ignoreChatMessage;

    @Shadow
    public abstract void sendChatMessage(String content);

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String message, CallbackInfo ci) {
        if (ignoreChatMessage) return;
        if (!message.startsWith(CommandManager.INSTANCE.getPrefix())) {
            SendMessageEvent event = EVENT_BUS.post(new SendMessageEvent(message));
            if (!event.isCancelled()) {
                ignoreChatMessage = true;
                sendChatMessage(event.getMessage());
                ignoreChatMessage = false;
            }
            ci.cancel();
            return;
        };

        try {
            CommandManager.dispatch(message.substring(CommandManager.INSTANCE.getPrefix().length()));
        } catch (CommandSyntaxException e) {
            ChatUtil.error(e.getMessage());
        }

        mc.inGameHud.getChatHud().addToMessageHistory(message);
        ci.cancel();
    }

    @Redirect(method = "onEntityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;showFloatingItem(Lnet/minecraft/item/ItemStack;)V"))
    private void noRenderTotemPop(GameRenderer instance, ItemStack floatingItem) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (!noRender.isEnabled() || noRender.totemPop.isEnabled()) mc.gameRenderer.showFloatingItem(Items.TOTEM_OF_UNDYING.getDefaultStack());
    }
}
