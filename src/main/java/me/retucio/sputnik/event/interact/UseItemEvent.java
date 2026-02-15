package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftClientMixin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;


/**
 * @see MinecraftClientMixin#onUseItem
 */
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
