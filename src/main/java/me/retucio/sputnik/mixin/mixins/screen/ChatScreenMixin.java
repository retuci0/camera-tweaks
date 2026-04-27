package me.retucio.sputnik.mixin.mixins.screen;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ChatPlus;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatScreen.class, priority = 1001)
public abstract class ChatScreenMixin {

    @Shadow
    protected EditBox input;

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo info) {
        ChatPlus chatPlus = ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class);
        if (chatPlus.isEnabled() && chatPlus.noCharLimit.getValue()) input.setMaxLength(Integer.MAX_VALUE);
    }
}