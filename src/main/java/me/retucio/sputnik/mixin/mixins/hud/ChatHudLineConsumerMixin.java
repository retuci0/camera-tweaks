package me.retucio.sputnik.mixin.mixins.hud;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ChatPlus;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.hud.ChatHud$1", remap = false)
public abstract class ChatHudLineConsumerMixin {

    @Inject(method = "accept", at = @At("HEAD"))
    private void setLine(ChatHudLine.Visible line, int y, float opacity, CallbackInfo ci) {
        ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class).line = line;
    }
}
