package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AttackBlockEvent extends Event {

    private final BlockPos pos;
    private final Direction dir;

    public AttackBlockEvent(BlockPos pos, Direction dir) {
        this.pos = pos;
        this.dir = dir;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDir() {
        return dir;
    }
}
