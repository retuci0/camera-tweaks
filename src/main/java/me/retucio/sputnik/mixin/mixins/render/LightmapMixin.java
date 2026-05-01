package me.retucio.sputnik.mixin.mixins.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Fullbright;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public abstract class LightmapMixin {

    @Shadow @Final
    private GpuTexture texture;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void makeThingsBright(LightmapRenderState renderState, CallbackInfo ci) {
        if (ModuleManager.INSTANCE.getModuleByClass(Fullbright.class).isEnabled()
                && ModuleManager.INSTANCE.getModuleByClass(Fullbright.class).mode.is(Fullbright.Modes.GAMMA))
        {
            ProfilerFiller profiler = Profiler.get();
            profiler.push("lightmap");

            RenderSystem.getDevice().createCommandEncoder()
                    .clearColorTexture(texture, ARGB.color(255, 255, 255, 255));

            profiler.pop();
            ci.cancel();
        }
    }
}
