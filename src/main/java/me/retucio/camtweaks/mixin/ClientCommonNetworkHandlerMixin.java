package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.mixin.accessor.ConfirmServerResourcePackScreenAccessor;
import me.retucio.camtweaks.mixin.accessor.PackAccessor;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.network.RPackBypass;
import me.retucio.camtweaks.util.interfaces.IConfirmScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {

    @Shadow @Final protected MinecraftClient client;
    @Shadow @Final protected ServerInfo serverInfo;
    @Shadow @Final protected ClientConnection connection;

    @Unique  // tardar 3 segundos porque algunos servers detectar el tiempo entre que se acepta y se carga el pack
    private final Executor DELAYED_EXECUTOR = CompletableFuture.delayedExecutor(3L, TimeUnit.SECONDS);

    @Inject(method = "createConfirmServerResourcePackScreen", at = @At("TAIL"))
    private void setScreenBypassAction(UUID id, URL url, String hash, boolean required, Text prompt, CallbackInfoReturnable<Screen> cir) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (!bypassPack.isEnabled()) return;

        final Screen screen = cir.getReturnValue();
        ((IConfirmScreen) screen).smegma$setBypassAction(() -> {
            client.setScreen(((ConfirmServerResourcePackScreenAccessor) screen).getParentScreen());
            if (this.serverInfo != null) {
                this.serverInfo.setResourcePackPolicy(bypassPack.getPolicy());
                this.client.getServerResourcePackProvider().acceptAll();
                ServerList.updateServerListEntry(this.serverInfo);
            }
            bypass(((ConfirmServerResourcePackScreenAccessor) screen).getRequests().stream().map(PackAccessor::callId).toList());
        });
    }

    @Unique
    public void bypass(List<UUID> ids) {
        ids.forEach(id -> connection.send(new ResourcePackStatusC2SPacket(id, ResourcePackStatusC2SPacket.Status.ACCEPTED)));
        ids.forEach(id -> connection.send(new ResourcePackStatusC2SPacket(id, ResourcePackStatusC2SPacket.Status.DOWNLOADED)));
        DELAYED_EXECUTOR.execute(() ->
                ids.forEach(id -> connection.send(new ResourcePackStatusC2SPacket(id, ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED)))
        );
    }
}
