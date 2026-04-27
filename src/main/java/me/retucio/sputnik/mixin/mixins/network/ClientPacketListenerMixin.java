package me.retucio.sputnik.mixin.mixins.network;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.event.network.SendMessageEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.NoRender;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.sputnik.Sputnik.*;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Unique
    private boolean ignoreChatMessage;

    @Shadow
    public abstract void sendChat(String content);

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String content, CallbackInfo ci) {
        if (ignoreChatMessage) return;
        if (!content.startsWith(CommandManager.INSTANCE.getPrefix())) {
            SendMessageEvent event = EVENT_BUS.post(new SendMessageEvent(content));
            if (!event.isCancelled()) {
                ignoreChatMessage = true;
                sendChat(event.getMessage());
                ignoreChatMessage = false;
            }
            ci.cancel();
            return;
        };

        try {
            CommandManager.dispatch(content.substring(CommandManager.INSTANCE.getPrefix().length()));
        } catch (CommandSyntaxException e) {
            ChatUtil.error(e.getMessage());
        }

        mc.gui.getChat().addRecentChat(content);
        ci.cancel();
    }

    @Redirect(method = "handleEntityEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;displayItemActivation(Lnet/minecraft/world/item/ItemStack;)V"))
    private void noRenderTotemPop(GameRenderer instance, ItemStack itemStack) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (!noRender.isEnabled() || noRender.totemPop.getValue())
            mc.gameRenderer.displayItemActivation(itemStack);
    }
}
