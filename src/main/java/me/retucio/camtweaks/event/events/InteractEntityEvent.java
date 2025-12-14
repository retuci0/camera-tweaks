package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

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