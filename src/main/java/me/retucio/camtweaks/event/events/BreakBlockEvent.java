package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.util.math.BlockPos;


/**
 * @see me.retucio.camtweaks.mixin.ClientPlayerInteractionManagerMixin#onBlockBreak
 */
public class BreakBlockEvent extends Event {

    private BlockPos pos;

    public BreakBlockEvent(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
}
