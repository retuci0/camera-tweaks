package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
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
