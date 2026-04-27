package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftMixin;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


/**
 * @see MinecraftMixin#onUseItem
 */
public class UseItemEvent extends Event {

    private ItemStack stack;
    private InteractionHand hand;

    public UseItemEvent(ItemStack stack, InteractionHand hand) {
        this.stack = stack;
        this.hand = hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public Item getItem() {
        return stack.getItem();
    }

    public InteractionHand getHand() {
        return hand;
    }

    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }
}
