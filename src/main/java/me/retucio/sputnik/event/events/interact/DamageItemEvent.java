package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.item.ItemStackMixin;
import net.minecraft.item.ItemStack;


/**
 * @see ItemStackMixin#onDamage
 */
public class DamageItemEvent extends Event {

    private final int amount;
    private final ItemStack stack;

    public DamageItemEvent(int amount, ItemStack stack) {
        this.amount = amount;
        this.stack = stack;
    }

    public int getAmount() {
        return amount;
    }

    public ItemStack getStack() {
        return stack;
    }
}
