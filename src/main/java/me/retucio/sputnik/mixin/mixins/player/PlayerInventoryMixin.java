package me.retucio.sputnik.mixin.mixins.player;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.interact.SwitchSlotEvent;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow
    private int selected;

    @Inject(method = "setSelectedSlot", at = @At("HEAD"), cancellable = true)
    private void onSwitchSlot(int selected, CallbackInfo ci) {
        if (this.selected == selected) return;
        SwitchSlotEvent event = Sputnik.EVENT_BUS.post(new SwitchSlotEvent(this.selected, selected));
        if (event.isCancelled()) ci.cancel();
        if (event.getSlot() != selected) this.selected = event.getSlot();
    }
}
