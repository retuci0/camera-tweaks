package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;


/**
 * @see me.retucio.sputnik.mixin.ClientPlayerInteractionManagerMixin#onEntityInteract
 */
public class InteractEntityEvent extends Event {

    private final Entity entity;
    private Hand hand;

    public InteractEntityEvent(Entity entity, Hand hand) {
        this.entity = entity;
        this.hand = hand;
    }

    public Entity getEntity() {
        return entity;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }
}