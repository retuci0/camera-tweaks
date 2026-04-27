package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Fullbright;
import me.retucio.sputnik.module.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapTextureManagerMixin {

    @Unique Fullbright fullbright;
    @Unique NoRender noRender;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        fullbright = ModuleManager.INSTANCE.getModuleByClass(Fullbright.class);
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", shift = At.Shift.AFTER), cancellable = true)
    private void update(LightmapRenderState renderState, float partialTicks, CallbackInfo ci, @Local(name = "profiler") ProfilerFiller profiler) {
        if (fullbright.isEnabled() && fullbright.mode.is(Fullbright.Modes.GAMMA)) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(Minecraft.getInstance().gameRenderer.lightmap().texture(),
                ARGB.color(
                    fullbright.color.getA(),
                    fullbright.color.getR(),
                    fullbright.color.getG(),
                    fullbright.color.getB()
                )
            );
            profiler.pop();
            ci.cancel();
        }
    }

    @Inject(method = "calculateDarknessScale", at = @At("HEAD"), cancellable = true)
    private void noRenderDarkness(LivingEntity camera, float darknessGamma, float partialTickTime, CallbackInfoReturnable<Float> cir) {
        if (noRender.isEnabled() && !noRender.darknessEffect.getValue()) cir.setReturnValue(0f);
    }
}