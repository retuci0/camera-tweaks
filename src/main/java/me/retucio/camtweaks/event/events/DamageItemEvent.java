package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.item.ItemStack;


/**
 * @see me.retucio.camtweaks.mixin.ItemStackMixin#onDamage
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
