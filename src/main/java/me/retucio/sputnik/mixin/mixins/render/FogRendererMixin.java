package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.NoRender;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;


@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Unique
    NoRender noRender;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;isApplicable(Lnet/minecraft/world/level/material/FogType;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean noRenderFogs(FogEnvironment instance, FogType fogType, Entity entity) {
        if (!noRender.isEnabled()) return instance.isApplicable(fogType, entity);
        String className = instance.getClass().getSimpleName();

        if ((className.equals("BlindnessEffectFogModifier") && !noRender.blindnessEffect.getValue())
                || (className.equals("DarknessEffectFogModifier") && !noRender.darknessEffect.getValue())
                || ((className.equals("LavaFogModifier") || className.equals("WaterFogModifier")
                    || className.equals("PowederSnowFogModifier")) && !noRender.fluidOverlay.getValue()))
        {
            return false;
        }

        return instance.isApplicable(fogType, entity);
    }

    @ModifyArgs(method = "computeFogColor", at = @At(value = "INVOKE", target = "Lorg/joml/Vector4f;set(FFFF)Lorg/joml/Vector4f;"))
    private void modifyLavaFog(Args args, @Local(argsOnly = true, name = "dest") Vector4f original, @Local(argsOnly = true, name = "camera") Camera camera) {
        if (noRender.isEnabled() && !noRender.fluidOverlay.getValue() &&
                (camera.getFluidInCamera() == FogType.LAVA
                        || camera.getFluidInCamera() == FogType.WATER))
        {
            args.set(0, original.x);
            args.set(1, original.y);
            args.set(2, original.z);
            args.set(3, 0f);
        }
    }

    @ModifyExpressionValue(method = "computeFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getWaterVision()F"))
    private float noRenderUnderwaterFog(float original) {
        return (noRender.isEnabled() && !noRender.fluidOverlay.getValue()) ? 0 : original;
    }
}
