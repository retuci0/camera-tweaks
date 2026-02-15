package me.retucio.sputnik.event.network;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.world.ClientWorldMixin;
import net.minecraft.entity.Entity;


/**
 * @see ClientWorldMixin#onRemoveEntity
 */
public class RemoveEntityEvent extends Event {

    private final Entity entity;

    public RemoveEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
