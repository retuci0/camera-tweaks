package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.item.property.numeric.CompassState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(CompassState.class)
public abstract class CompassStateMixin {

    @Unique
    private static Freecam freecam;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(boolean wobble, CompassState.Target target, CallbackInfo ci) {
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
    }

    @ModifyExpressionValue(method = "getBodyYaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getBodyYaw()F"))
    private static float getBodyYaw(float original) {
        if (freecam.isEnabled()) return mc.gameRenderer.getCamera().getYaw();
        return original;
    }

    @ModifyReturnValue(method = "getAngleTo(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)D", at = @At("RETURN"))
    private static double getAngleTo(double original, Entity entity, BlockPos blockPos) {
        if (freecam.isEnabled()) {
            Vec3d vec3d = Vec3d.ofCenter(blockPos);
            Camera camera = mc.gameRenderer.getCamera();
            return Math.atan2(vec3d.getZ() - camera.getPos().z, vec3d.getX() - camera.getPos().x) / (float) (Math.PI * 2);
        }
        return original;
    }
}