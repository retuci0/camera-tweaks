package me.retucio.sputnik.event.events.network;

import me.retucio.sputnik.event.Event;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.DisconnectionInfo;


/**
 * @see me.retucio.sputnik.mixin.ClientConnectionMixin#onDisconnect
 */
public class DisconnectEvent extends Event {

    private final DisconnectionInfo info;
    private final ServerInfo server;

    public DisconnectEvent(DisconnectionInfo info, ServerInfo server) {
        this.info = info;
        this.server = server;
    }

    public DisconnectionInfo getInfo() {
        return info;
    }

    public ServerInfo getServer() {
        return server;
    }
}