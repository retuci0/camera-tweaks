package me.retucio.sputnik.event.events;

import me.retucio.sputnik.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class BlockShapeEvent extends Event {

    private BlockState state;
    private BlockPos pos;
    private VoxelShape shape;

    public BlockShapeEvent(BlockState state, BlockPos pos, VoxelShape shape) {
        this.state = state;
        this.pos = pos;
        this.setShape(shape);
    }

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public VoxelShape getShape() {
        return shape;
    }

    public void setShape(VoxelShape shape) {
        this.shape = shape;
    }
}