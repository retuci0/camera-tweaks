package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.player.MultiPlayerGameModeMixin;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;


/**
 * @see MultiPlayerGameModeMixin#onEntityInteract
 */

public class InteractEntityEvent extends Event {

    private final Entity entity;
    private InteractionHand hand;

    public InteractEntityEvent(Entity entity, InteractionHand hand) {
        this.entity = entity;
        this.hand = hand;
    }

    public Entity getEntity() {
        return entity;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }
}