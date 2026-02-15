package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
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
