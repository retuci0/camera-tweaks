package me.retucio.sputnik.event.events.network;

import me.retucio.sputnik.event.Event;
import net.minecraft.entity.Entity;


/**
 * @see me.retucio.sputnik.mixin.ClientWorldMixin#onRemoveEntity
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
