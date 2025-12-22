package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class UseItemEvent extends Event {

    private ItemStack stack;
    private Hand hand;

    public UseItemEvent(ItemStack stack, Hand hand) {
        this.stack = stack;
        this.hand = hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }
}
