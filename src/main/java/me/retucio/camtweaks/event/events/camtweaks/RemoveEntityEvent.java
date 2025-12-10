package me.retucio.camtweaks.event.events.camtweaks;

import me.retucio.camtweaks.event.Event;
import net.minecraft.entity.Entity;

public class RemoveEntityEvent extends Event {

    private final Entity entity;

    public RemoveEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
