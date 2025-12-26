package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.DisconnectEvent;
import me.retucio.camtweaks.module.Module;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class Reconnect extends Module {

    private ServerInfo lastServer = null;

    public Reconnect() {
        super("reconectar", "añade un botón para reconectarse a un servidor tras ser desconectado");
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {
        lastServer = event.getServer();
    }

    public void reconnect() {
        if (lastServer == null) return;
        ConnectScreen.connect(
                null,
                mc,
                ServerAddress.parse(lastServer.address),
                lastServer,
                false,
                null
        );
    }
}
