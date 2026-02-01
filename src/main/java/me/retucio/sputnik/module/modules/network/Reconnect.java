package me.retucio.sputnik.module.modules.network;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.network.DisconnectEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class Reconnect extends Module {

    private ServerInfo lastServer = null;

    public Reconnect() {
        super("reconectar",
                "añade un botón para reconectarse a un servidor tras ser desconectado",
                Category.NETWORK);
    }

    @SubscribeEvent
    private void onDisconnect(DisconnectEvent event) {
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
