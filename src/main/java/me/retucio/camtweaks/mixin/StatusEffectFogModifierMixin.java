package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.NoRender;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StatusEffectFogModifier.class)
public abstract class StatusEffectFogModifierMixin {

    @Shadow
    public abstract RegistryEntry<StatusEffect> getStatusEffect();

    @ModifyReturnValue(method = "shouldApply", at = @At("RETURN"))
    public boolean noRenderBadEffects(boolean original) {
        NoRender noRender = ModuleManager.INSTANCE.getModuleByClass(NoRender.class);
        if (!noRender.isEnabled()) return original;
        if (getStatusEffect() == StatusEffects.BLINDNESS && !noRender.blindnessEffect.isEnabled()) return false;
        if (getStatusEffect() == StatusEffects.DARKNESS && !noRender.darknessEffect.isEnabled()) return false;
        return original;
    }
}
