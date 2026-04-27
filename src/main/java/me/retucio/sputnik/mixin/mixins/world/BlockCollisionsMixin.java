package me.retucio.sputnik.mixin.mixins.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.interact.BlockShapeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.retucio.sputnik.Sputnik.mc;

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin {

    @WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/CollisionContext;getCollisionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape onGetBlockShape(CollisionContext instance, BlockState state, CollisionGetter collisionGetter, BlockPos pos, Operation<VoxelShape> original) {
        VoxelShape shape = original.call(instance, state, collisionGetter, pos);

        if (collisionGetter != mc.level) {
            return shape;
        }

        BlockShapeEvent event = Sputnik.EVENT_BUS.post(new BlockShapeEvent(state, pos, shape));
        return event.isCancelled() ? Shapes.empty() : event.getShape();
    }
}
