package me.retucio.sputnik.mixin.mixins.screen;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.network.Reconnect;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;

import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin {

    @Shadow @Final
    private LinearLayout layout;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 2, shift = At.Shift.AFTER))
    private void addReconnectButton(CallbackInfo ci) {
        Reconnect reconnect = ModuleManager.INSTANCE.getModuleByClass(Reconnect.class);
        if (reconnect == null || !reconnect.isEnabled()) return;

        layout.addChild(Button.builder(
                Component.literal("reconectarse"),
                button -> reconnect.reconnect()
        ).build());
    }
}
