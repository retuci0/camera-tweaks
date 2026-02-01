package me.retucio.sputnik.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.events.interact.BlockShapeEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.retucio.sputnik.Sputnik.mc;

@Mixin(BlockCollisionSpliterator.class)
public abstract class BlockCollisionSpliteratorMixin {

    @WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/ShapeContext;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape onGetBlockShape(ShapeContext instance, BlockState state, CollisionView collisionView, BlockPos pos, Operation<VoxelShape> original) {
        VoxelShape shape = original.call(instance, state, collisionView, pos);

        if (collisionView != mc.world) {
            return shape;
        }

        BlockShapeEvent event = Sputnik.EVENT_BUS.post(new BlockShapeEvent(state, pos, shape));
        return event.isCancelled() ? VoxelShapes.empty() : event.getShape();
    }
}
