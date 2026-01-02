package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.camera.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.ChunkBorderDebugRenderer;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkBorderDebugRenderer.class)
public abstract class ChunkBorderDebugRendererMixin {

    @Shadow @Final
    private MinecraftClient client;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/ChunkSectionPos;"))
    private ChunkSectionPos getChunkPos(ChunkSectionPos chunkPos) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        if (!freecam.isEnabled()) return chunkPos;

        float delta = client.getRenderTickCounter().getTickProgress(true);
        return ChunkSectionPos.from(
                ChunkSectionPos.getSectionCoord(MathHelper.floor(freecam.getX(delta))),
                ChunkSectionPos.getSectionCoord(MathHelper.floor(freecam.getY(delta))),
                ChunkSectionPos.getSectionCoord(MathHelper.floor(freecam.getZ(delta)))
        );
    }
}
