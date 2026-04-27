package me.retucio.sputnik.mixin.mixins.hud;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ChatPlus;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1", remap = false)
public abstract class ChatComponentLineConsumerMixin {

    @Inject(method = "accept", at = @At("HEAD"))
    private void setLine(GuiMessage.Line line, int lineIndex, float alpha, CallbackInfo ci) {
        ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class).line = line;
    }
}
