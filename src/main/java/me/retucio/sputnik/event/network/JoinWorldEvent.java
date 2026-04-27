package me.retucio.sputnik.event.network;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftMixin;
import net.minecraft.client.multiplayer.ClientLevel;


/**
 * @see MinecraftMixin#onJoinWorld
 */

public class JoinWorldEvent extends Event {

    private final ClientLevel world;

    public JoinWorldEvent(ClientLevel world) {
        this.world = world;
    }

    public ClientLevel getWorld() {
        return world;
    }
}
