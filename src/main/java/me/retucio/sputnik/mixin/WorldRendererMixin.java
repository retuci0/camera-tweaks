package me.retucio.sputnik.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.events.Render3DEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.BlockOutline;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.render.NoRender;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;
import static me.retucio.sputnik.Sputnik.mc;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;updateCamera(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;Z)V"), index = 2)
    private boolean renderSetupTerrainModifyArg(boolean spectator) {
        return ModuleManager.INSTANCE.getModuleByClass(Freecam.class).isEnabled() || spectator;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci, @Local Profiler profiler) {
        if (mc == null || mc.world == null) return;

        MatrixStack matrices = new MatrixStack();
        matrices.push();

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180f));

        profiler.push(Sputnik.MOD_ID + "-3d");
        EVENT_BUS.post(new Render3DEvent(matrices, tickCounter, camera));

        profiler.pop();
        matrices.pop();
    }

    @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"), cancellable = true)
    private void noRenderBlockOutlinesFreecam(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, boolean renderBlockOutline, WorldRenderState renderStates, CallbackInfo ci) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        if (freecam.isEnabled() && !freecam.blockOutlines.getValue()) ci.cancel();
    }

    @Inject(method = "hasBlindnessOrDarkness(Lnet/minecraft/client/render/Camera;)Z", at = @At("HEAD"), cancellable = true)
    private void hasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (!noRender.isEnabled()) return;
        if (!noRender.blindnessEffect.getValue() || !noRender.darknessEffect.getValue()) cir.setReturnValue(null);
    }


    @ModifyArg(method = "renderTargetBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;DDDLnet/minecraft/client/render/state/OutlineRenderState;IF)V"))
    private int modifyBlockOutlineColor(int original) {
        BlockOutline outline = ModuleManager.INSTANCE.getModuleByClass(BlockOutline.class);
        if (!outline.isEnabled()) return original;
        return outline.color.getRGB();
    }

    @ModifyArg(method = "renderTargetBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;DDDLnet/minecraft/client/render/state/OutlineRenderState;IF)V"))
    private float modifyBlockOutlineLineWidth(float original) {
        BlockOutline outline = ModuleManager.INSTANCE.getModuleByClass(BlockOutline.class);
        if (!outline.isEnabled()) return original;
        return outline.lineWidth.getFloatValue();
    }
}