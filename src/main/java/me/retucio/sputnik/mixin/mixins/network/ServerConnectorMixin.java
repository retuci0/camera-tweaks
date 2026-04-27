package me.retucio.sputnik.mixin.mixins.network;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.network.RPackBypass;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.server.ServerPackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(targets = "net.minecraft.client.gui.screens.ConnectScreen$1")
public abstract class ServerConnectorMixin {

    @Inject(method = "convertPackStatus", at = @At("HEAD"), cancellable = true)
    private static void guardSwitchCase(ServerData.ServerPackStatus resourcePackStatus, CallbackInfoReturnable<ServerPackManager.PackPromptStatus> cir) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (!bypassPack.isEnabled()) return;
        if (resourcePackStatus == bypassPack.getStatus())
            cir.setReturnValue(ServerPackManager.PackPromptStatus.ALLOWED);
    }
}
