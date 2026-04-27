package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkBorderRenderer.class)
public abstract class ChunkBorderRendererMixin {

    @Shadow @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "emitGizmos", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/SectionPos;"))
    private SectionPos getChunkPos(SectionPos chunkPos) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        if (!freecam.isEnabled()) return chunkPos;

        float delta = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        return SectionPos.of(
                SectionPos.posToSectionCoord(Mth.floor(freecam.getX(delta))),
                SectionPos.posToSectionCoord(Mth.floor(freecam.getY(delta))),
                SectionPos.posToSectionCoord(Mth.floor(freecam.getZ(delta)))
        );
    }
}
