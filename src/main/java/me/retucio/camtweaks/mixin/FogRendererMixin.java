package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.render.NoRender;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.entity.Entity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Unique
    NoRender noRender;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Redirect(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogModifier;shouldApply(Lnet/minecraft/block/enums/CameraSubmersionType;Lnet/minecraft/entity/Entity;)Z"))
    private boolean noRenderFogs(FogModifier instance, CameraSubmersionType cameraSubmersionType, Entity entity) {
        if (!noRender.isEnabled()) return instance.shouldApply(cameraSubmersionType, entity);
        String className = instance.getClass().getSimpleName();

        if ((className.equals("BlindnessEffectFogModifier") && !noRender.blindnessEffect.isEnabled())
                || (className.equals("DarknessEffectFogModifier") && !noRender.darknessEffect.isEnabled())
                || ((className.equals("LavaFogModifier") || className.equals("WaterFogModifier")
                        || className.equals("PowederSnowFogModifier")) && !noRender.fluidOverlay.isEnabled()))
            return false;

        return instance.shouldApply(cameraSubmersionType, entity);
    }

    @ModifyReturnValue(method = "getFogColor", at = @At("RETURN"))
    private Vector4f modifyLavaFog(Vector4f original, Camera camera) {
        if (noRender.isEnabled() && !noRender.fluidOverlay.isEnabled() &&
                (camera.getSubmersionType() == CameraSubmersionType.LAVA
                        || camera.getSubmersionType() == CameraSubmersionType.WATER))
            return new Vector4f(original.x, original.y, original.z, 0f);

        return original;
    }

    @ModifyExpressionValue(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getUnderwaterVisibility()F"))
    private float noRenderUnderwaterFog(float original) {
        return (noRender.isEnabled() && !noRender.fluidOverlay.isEnabled()) ? 0 : original;
    }
}
