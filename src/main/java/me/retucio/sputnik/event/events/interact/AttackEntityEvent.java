package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.player.ClientPlayerInteractionManagerMixin;
import net.minecraft.entity.Entity;


/**
 * @see ClientPlayerInteractionManagerMixin#onAttackEntity
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
