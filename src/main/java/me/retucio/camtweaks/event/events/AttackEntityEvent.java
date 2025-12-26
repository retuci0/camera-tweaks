package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.entity.Entity;


/**
 * @see me.retucio.camtweaks.mixin.ClientPlayerInteractionManagerMixin#onAttackEntity
 */
public class AttackEntityEvent extends Event {

    private final Entity entity;

    public AttackEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
