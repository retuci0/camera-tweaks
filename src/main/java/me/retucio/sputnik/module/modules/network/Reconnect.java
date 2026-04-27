package me.retucio.sputnik.module.modules.network;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.DisconnectEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;


public class Reconnect extends Module {

    private ServerData lastServer = null;

    public Reconnect() {
        super("reconectar",
                "añade un botón para reconectarse a un servidor tras ser desconectado",
                Category.NETWORK);
    }

    @EventListener
    private void onDisconnect(DisconnectEvent event) {
        lastServer = event.getServer();
    }

    public void reconnect() {
        if (lastServer == null) return;
        ConnectScreen.startConnecting(
                null,
                mc,
                ServerAddress.parseString(lastServer.ip),
                lastServer,
                false,
                null
        );
    }
}
