package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.player.ClientPlayerInteractionManagerMixin;
import net.minecraft.util.math.BlockPos;


/**
 * @see ClientPlayerInteractionManagerMixin#onBlockBreak
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
