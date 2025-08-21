package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.modules.BetterChat;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.moduleManager;

@Mixin(value = ChatScreen.class, priority = 1001)
public class ChatScreenMixin {

    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo info) {
        BetterChat betterChat = moduleManager.getModuleByClass(BetterChat.class);
        if (betterChat.isEnabled() && betterChat.noCharLimit.isEnabled()) chatField.setMaxLength(Integer.MAX_VALUE);
    }
}