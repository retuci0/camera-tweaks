package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;
import net.minecraft.entity.Entity;


/**
 * @see me.retucio.sputnik.mixin.ClientPlayerInteractionManagerMixin#onAttackEntity
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
