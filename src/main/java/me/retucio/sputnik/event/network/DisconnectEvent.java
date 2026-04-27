package me.retucio.sputnik.event.network;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.network.ConnectionMixin;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.DisconnectionDetails;


/**
 * @see ConnectionMixin#onDisconnect
 */
public class DisconnectEvent extends Event {

    private final DisconnectionDetails details;
    private final ServerData server;

    public DisconnectEvent(DisconnectionDetails details, ServerData server) {
        this.details = details;
        this.server = server;
    }

    public DisconnectionDetails getDetails() {
        return details;
    }

    public ServerData getServer() {
        return server;
    }
}