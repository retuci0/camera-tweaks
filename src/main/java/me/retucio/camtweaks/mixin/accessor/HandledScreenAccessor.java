package me.retucio.camtweaks.mixin.accessor;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

    @Accessor("focusedSlot")
    Slot getFocusedSlot();

    @Accessor("doubleClicking")
    void setDoubleClicking(boolean doubleClicking);
}
