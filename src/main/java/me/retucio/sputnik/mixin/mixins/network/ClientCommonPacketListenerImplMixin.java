package me.retucio.sputnik.mixin.mixins.network;

import me.retucio.sputnik.mixin.accessors.PackConfirmScreenAccessor;
import me.retucio.sputnik.mixin.accessors.PendingRequestAccessor;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.network.RPackBypass;
import me.retucio.sputnik.util.interfaces.IConfirmScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
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


@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {

    @Shadow @Final protected Minecraft minecraft;
    @Shadow @Final protected ServerData serverData;
    @Shadow @Final protected Connection connection;

    @Unique  // tardar 3 segundos porque algunos servers detectar el tiempo entre que se acepta y se carga el pack
    private final Executor DELAYED_EXECUTOR = CompletableFuture.delayedExecutor(3L, TimeUnit.SECONDS);

    @Inject(method = "addOrUpdatePackPrompt", at = @At("TAIL"))
    private void setScreenBypassAction(UUID packId, URL url, String hash, boolean required, Component prompt, CallbackInfoReturnable<Screen> cir) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (!bypassPack.isEnabled()) return;

        final Screen screen = cir.getReturnValue();
        ((IConfirmScreen) screen).sputnik$setBypassAction(() -> {
            minecraft.setScreen(((PackConfirmScreenAccessor) screen).getParentScreen());
            if (serverData != null) {
                serverData.setResourcePackStatus(bypassPack.getStatus());
                minecraft.getDownloadedPackSource().allowServerPacks();
                ServerList.saveSingleServer(this.serverData);
            }
            bypass(((PackConfirmScreenAccessor) screen).getRequests().stream().map(PendingRequestAccessor::callId).toList());
        });
    }

    @Unique
    public void bypass(List<UUID> ids) {
        ids.forEach(id -> connection.send(new ServerboundResourcePackPacket(id, ServerboundResourcePackPacket.Action.ACCEPTED)));
        ids.forEach(id -> connection.send(new ServerboundResourcePackPacket(id, ServerboundResourcePackPacket.Action.DOWNLOADED)));
        DELAYED_EXECUTOR.execute(() ->
                ids.forEach(id -> connection.send(new ServerboundResourcePackPacket(id, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED)))
        );
    }
}
