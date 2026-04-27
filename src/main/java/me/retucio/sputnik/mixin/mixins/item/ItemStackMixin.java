package me.retucio.sputnik.mixin.mixins.item;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.interact.DamageItemEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;


@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract int getDamageValue();

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void onDamage(int newDamage, @Nullable ServerPlayer player, Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        Sputnik.EVENT_BUS.post(new DamageItemEvent(getDamageValue(), stack));
    }
}
