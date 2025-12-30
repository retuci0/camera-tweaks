package me.retucio.camtweaks.event;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * @see me.retucio.camtweaks.mixin.MinecraftClientMixin#onJoinWorld 
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
