package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.entity.Entity;


/**
 * @see me.retucio.camtweaks.mixin.ClientWorldMixin#onAddEntity
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
