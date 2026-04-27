package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.NoRender;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(MobEffectFogEnvironment.class)
public abstract class MobEffectFogEnvironmentMixin {

    @Shadow
    public abstract Holder<MobEffect> getMobEffect();

    @ModifyReturnValue(method = "isApplicable", at = @At("RETURN"))
    public boolean noRenderBadEffects(boolean original) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (!noRender.isEnabled()) return original;
        if (getMobEffect() == MobEffects.BLINDNESS && !noRender.blindnessEffect.getValue()) return false;
        if (getMobEffect() == MobEffects.DARKNESS && !noRender.darknessEffect.getValue()) return false;
        return original;
    }
}
