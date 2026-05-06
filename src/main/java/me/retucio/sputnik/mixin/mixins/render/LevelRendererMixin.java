package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.event.render.RenderBlockOutlineEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;
import static me.retucio.sputnik.Sputnik.mc;


@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    // renderLevel()??
    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;cullTerrain(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Z)V"), index = 2)
    private boolean renderSetupTerrainModifyArg(boolean spectator) {
        return ModuleManager.INSTANCE.getModuleByClass(Freecam.class).isEnabled() || spectator;
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void render(GraphicsResourceAllocator resourceAllocator, DeltaTracker dt, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci, @Local(name = "profiler") ProfilerFiller profiler) {
        if (mc == null || mc.level == null) return;

        PoseStack matrices = new PoseStack();
        matrices.pushPose();

        matrices.mulPose(Axis.XP.rotationDegrees(cameraState.xRot));
        matrices.mulPose(Axis.YP.rotationDegrees(cameraState.yRot + 180f));

        profiler.push(Sputnik.MOD_ID + "-3d");
        EVENT_BUS.post(new Render3DEvent(matrices, dt, cameraState));

        profiler.pop();
        matrices.popPose();
    }

    @Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void noRenderBlockOutlinesFreecam(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean onlyTranslucentBlocks, LevelRenderState levelRenderState, CallbackInfo ci) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        if (freecam.isEnabled() && !freecam.blockOutlines.getValue()) ci.cancel();
    }

//    @Inject(method = "hasBlindnessOrDarkness(Lnet/minecraft/client/extractRenderState/Camera;)Z", at = @At("HEAD"), cancellable = true)
//    private void hasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
//        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
//        if (!noRender.isEnabled()) return;
//        if (!noRender.blindnessEffect.getValue() || !noRender.darknessEffect.getValue()) cir.setReturnValue(null);
//    }

    // puto mojang con sus nombres confusos
    @ModifyArgs(method = "renderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IF)V"))
    private void modifyBlockOutline(Args args) {
        // SIX SEVEN!!! (me pegan en casa)
        RenderBlockOutlineEvent event = EVENT_BUS.post(new RenderBlockOutlineEvent(args.get(6), args.get(7)));
        if (event.getColor() != (int) args.get(6))
            args.set(6, event.getColor());
        if (event.getLineWidth() != (float) args.get(7))
            args.set(7, event.getLineWidth());
    }
}