package me.retucio.sputnik.event.events.network;

import me.retucio.sputnik.event.Event;
import net.minecraft.client.world.ClientWorld;


/**
 * @see me.retucio.sputnik.mixin.MinecraftClientMixin#onJoinWorld
 */
public class JoinWorldEvent extends Event {

    private final ClientWorld world;

    public JoinWorldEvent(ClientWorld world) {
        this.world = world;
    }

    public ClientWorld getWorld() {
        return world;
    }
}
