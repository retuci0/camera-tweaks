package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.DisconnectionInfo;


/**
 * @see me.retucio.camtweaks.mixin.ClientConnectionMixin#onDisconnect
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