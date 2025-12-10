package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.RenderWorldEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.BlockOutline;
import me.retucio.camtweaks.module.modules.Freecam;
import me.retucio.camtweaks.module.modules.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;
import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;method_74752(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;Z)V"), index = 2)
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

        profiler.push(CameraTweaks.MOD_ID + "-3d");
        EVENT_BUS.post(new RenderWorldEvent(matrices, tickCounter, camera));

        profiler.pop();
        matrices.pop();
    }

    @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"), cancellable = true)
    private void noRenderBlockOutlinesFreecam(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, boolean renderBlockOutline, WorldRenderState renderStates, CallbackInfo ci) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        if (freecam.isEnabled() && !freecam.blockOutlines.isEnabled()) ci.cancel();
    }

    @Inject(method = "hasBlindnessOrDarkness(Lnet/minecraft/client/render/Camera;)Z", at = @At("HEAD"), cancellable = true)
    private void hasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (!noRender.isEnabled()) return;
        if (!noRender.blindnessEffect.isEnabled() || !noRender.darknessEffect.isEnabled()) cir.setReturnValue(null);
    }

    @Redirect(method = "renderTargetBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ColorHelper;withAlpha(II)I"))
    private int modifyBlockOutlineColor(int alpha, int rgb) {
        BlockOutline outline = ModuleManager.INSTANCE.getModuleByClass(BlockOutline.class);
        if (!outline.isEnabled()) return ColorHelper.withAlpha(alpha, rgb);

        if (outline.rainbow.isEnabled()) {
            float speed = 10001 - outline.rainbowSpeed.getFloatValue();  // 10001 para evitar divisiones por cero
            float hue = (System.currentTimeMillis() % (int) speed) / speed;
            Color gamingProMax = Color.getHSBColor(hue, 1, 1);

            return new Color(
                    gamingProMax.getRed(),
                    gamingProMax.getGreen(),
                    gamingProMax.getBlue(),
                    outline.alpha.getIntValue()
            ).getRGB();
        } else {
            return new Color(
                    outline.red.getIntValue(),
                    outline.green.getIntValue(),
                    outline.blue.getIntValue(),
                    outline.alpha.getIntValue()
            ).getRGB();
        }
    }
}