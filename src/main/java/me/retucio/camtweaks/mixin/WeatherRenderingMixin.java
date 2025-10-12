package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.NoRender;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(WeatherRendering.class)
public abstract class WeatherRenderingMixin {

    @Unique
    NoRender noRender;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void getModules(CallbackInfo ci) {
        noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
    }

    @Inject(method = "addParticlesAndSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addParticleClient(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), cancellable = true)
    private void noRainParticles(ClientWorld world, Camera camera, int ticks, ParticlesMode particlesMode, CallbackInfo ci) {
        if (noRender.isEnabled() && !noRender.rain.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderPrecipitation", at = @At("HEAD"))
    private void noRenderRainAndSnow(VertexConsumerProvider vertexConsumers, Vec3d pos, WeatherRenderState weatherRenderState, CallbackInfo ci) {
        if (noRender.isEnabled()) {
            if (!noRender.rain.isEnabled()) weatherRenderState.rainPieces.clear();
            if (!noRender.snow.isEnabled()) weatherRenderState.snowPieces.clear();
        }
    }
}
