package me.retucio.sputnik.mixin.mixins.world;

import me.retucio.sputnik.event.network.ChunkOcclusionEvent;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;

@Mixin(ChunkOcclusionDataBuilder.class)
public abstract class ChunkOcclusionDataBuilderMixin {

    @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true)
    private void onChunkOcclusion(BlockPos pos, CallbackInfo ci) {
        ChunkOcclusionEvent event = EVENT_BUS.post(new ChunkOcclusionEvent());
        if (event.isCancelled()) ci.cancel();
    }
}
