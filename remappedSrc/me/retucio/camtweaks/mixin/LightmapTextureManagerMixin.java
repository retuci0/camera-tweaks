package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Fullbright;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Unique
    Fullbright fullbright;

    @Shadow @Final
    private GpuTexture glTexture;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(GameRenderer renderer, MinecraftClient client, CallbackInfo ci) {
        fullbright = ModuleManager.INSTANCE.getModuleByClass(Fullbright.class);
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", shift = At.Shift.AFTER), cancellable = true)
    private void update(float tickProgress, CallbackInfo ci, @Local Profiler profiler) {
        if (fullbright.isEnabled() && fullbright.mode.is(Fullbright.Modes.GAMMA)) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(glTexture, ColorHelper.getArgb(
                    fullbright.alpha.getIntValue(),
                    fullbright.red.getIntValue(),
                    fullbright.green.getIntValue(),
                    fullbright.blue.getIntValue()
            ));
            profiler.pop();
            ci.cancel();
        }
    }
}