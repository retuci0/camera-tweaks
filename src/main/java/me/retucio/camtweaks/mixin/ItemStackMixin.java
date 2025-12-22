package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.event.events.DamageItemEvent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "setDamage", at = @At("HEAD"))
    private void onDamage(int damage, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        EVENT_BUS.post(new DamageItemEvent(damage, stack));
    }
}
