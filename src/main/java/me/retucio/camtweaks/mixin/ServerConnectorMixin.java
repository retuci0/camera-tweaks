package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.RPackBypass;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.server.ServerResourcePackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screen.multiplayer.ConnectScreen$1")
public abstract class ServerConnectorMixin {

    @Inject(method = "toAcceptanceStatus", at = @At("HEAD"), cancellable = true)
    private static void guardSwitchCase(ServerInfo.ResourcePackPolicy policy, CallbackInfoReturnable<ServerResourcePackManager.AcceptanceStatus> cir) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (!bypassPack.isEnabled()) return;
        if (policy == bypassPack.getPolicy())
            cir.setReturnValue(ServerResourcePackManager.AcceptanceStatus.ALLOWED);
    }
}
