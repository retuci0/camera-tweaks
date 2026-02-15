package me.retucio.sputnik.event.network;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftClientMixin;
import net.minecraft.client.world.ClientWorld;


/**
 * @see MinecraftClientMixin#onJoinWorld
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
