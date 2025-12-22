package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.CritsPlus;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.DamageParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.awt.*;

@Mixin(DamageParticle.class)
public abstract class DamageParticleMixin {

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private BillboardParticle.RenderType makeCritsTranslucent(BillboardParticle.RenderType original) {
        if (ModuleManager.INSTANCE.getModuleByClass(CritsPlus.class).isEnabled()) return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
        return original;
    }
}


@Mixin(DamageParticle.Factory.class)
abstract class DamageParticleFactoryMixin {

    @ModifyVariable(method = "createParticle(Lnet/minecraft/particle/SimpleParticleType;Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/util/math/random/Random;)Lnet/minecraft/client/particle/Particle;", at = @At(value = "STORE"))
    private DamageParticle modifyCritColor(DamageParticle original) {
        CritsPlus crits = ModuleManager.INSTANCE.getModuleByClass(CritsPlus.class);
        if (!crits.isEnabled()) return original;

        if (crits.rainbow.isEnabled()) {
            Color gamning = Colors.rainbowColor(crits.rainbowSpeed.getIntValue(), crits.alpha.getIntValue());
            original.setColor(
                    gamning.getRed() / 255f,
                    gamning.getGreen() / 255f,
                    gamning.getBlue() / 255f);
        } else {
            original.setColor(
                    crits.red.getFloatValue() / 255f,
                    crits.green.getFloatValue() / 255f,
                    crits.blue.getFloatValue() / 255f);
        }

        original.alpha = crits.alpha.getFloatValue() / 255f;
        original.scale *= crits.scale.getFloatValue();
        original.velocityMultiplier *= crits.velocityMultipler.getFloatValue();
        original.gravityStrength *= crits.gravity.getFloatValue();
        original.setMaxAge(crits.maxAge.getIntValue());
        original.collidesWithWorld = crits.collide.isEnabled();

        return original;
    }
}