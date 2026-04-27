package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.NoRender;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ArrowLayer.class)
public abstract class ArrowLayerMixin {

    @ModifyReturnValue(method = "numStuck", at =  @At("RETURN"))
    private int noRenderStuckArrows(int original) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (noRender.isEnabled() && !noRender.stuckArrows.getValue()) return 0;
        return original;
    }
}
