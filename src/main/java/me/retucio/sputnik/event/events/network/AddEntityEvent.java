package me.retucio.sputnik.event.events.network;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.world.ClientWorldMixin;
import net.minecraft.entity.Entity;


/**
 * @see ClientWorldMixin#onAddEntity
 */
public class AddEntityEvent extends Event {

    private final Entity entity;

    public AddEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
