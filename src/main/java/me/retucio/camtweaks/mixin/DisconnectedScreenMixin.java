package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.network.Reconnect;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin {

    @Shadow @Final
    private DirectionalLayoutWidget grid;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 2, shift = At.Shift.AFTER))
    private void addReconnectButton(CallbackInfo ci) {
        Reconnect reconnect = ModuleManager.INSTANCE.getModuleByClass(Reconnect.class);
        if (reconnect == null || !reconnect.isEnabled()) return;

        grid.add(ButtonWidget.builder(
                Text.literal("reconectarse"),
                button -> reconnect.reconnect()
        ).build());
    }
}
