package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.event.events.DamageItemEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract int getDamage();

    @Inject(method = "onDurabilityChange", at = @At("TAIL"))
    private void onDamage(int damage, ServerPlayerEntity player, Consumer<Item> breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        EVENT_BUS.post(new DamageItemEvent(getDamage(), stack));
    }
}
