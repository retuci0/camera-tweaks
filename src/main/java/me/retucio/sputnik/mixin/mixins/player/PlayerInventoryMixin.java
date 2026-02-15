package me.retucio.sputnik.mixin.mixins.player;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.interact.SwitchSlotEvent;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow
    private int selectedSlot;

    @Inject(method = "setSelectedSlot", at = @At("HEAD"), cancellable = true)
    private void onSwitchSlot(int slot, CallbackInfo ci) {
        if (selectedSlot == slot) return;
        SwitchSlotEvent event = Sputnik.EVENT_BUS.post(new SwitchSlotEvent(selectedSlot, slot));
        if (event.isCancelled()) ci.cancel();
        if (event.getSlot() != slot) this.selectedSlot = event.getSlot();
    }
}
