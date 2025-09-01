package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.NoRender;
import net.minecraft.client.render.entity.feature.StuckArrowsFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StuckArrowsFeatureRenderer.class)
public abstract class StuckArrowsFeatureRendererMixin {

    @ModifyReturnValue(method = "getObjectCount", at =  @At("RETURN"))
    private int noRenderStuckArrows(int original) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (noRender.isEnabled() && !noRender.stuckArrows.isEnabled()) return 0;
        return original;
    }
}
