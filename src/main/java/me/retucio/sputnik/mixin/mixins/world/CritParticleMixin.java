package me.retucio.sputnik.mixin.mixins.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.CritsPlus;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CritParticle.class)
public abstract class CritParticleMixin {

    @ModifyReturnValue(method = "getLayer", at = @At("RETURN"))
    private SingleQuadParticle.Layer makeCritsTranslucent(SingleQuadParticle.Layer original) {
        if (ModuleManager.INSTANCE.getModuleByClass(CritsPlus.class).isEnabled())
            return SingleQuadParticle.Layer.TRANSLUCENT;

        return original;
    }
}


@Mixin(CritParticle.Provider.class)
abstract class CritParticleProviderMixin {

    @ModifyVariable(method = "createParticle(Lnet/minecraft/core/particles/SimpleParticleType;Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/util/RandomSource;)Lnet/minecraft/client/particle/Particle;", at = @At(value = "STORE"), name = "particle")
    private CritParticle modifyCritColor(CritParticle particle) {
        CritsPlus crits = ModuleManager.INSTANCE.getModuleByClass(CritsPlus.class);
        if (!crits.isEnabled()) return particle;

        particle.setColor(
                crits.color.getR() / 255f,
                crits.color.getG() / 255f,
                crits.color.getB() / 255f
        );

        particle.alpha = crits.color.getA() / 255f;
        particle.quadSize *= crits.scale.getFloatValue();
        particle.friction *= crits.velocityMultipler.getFloatValue();
        particle.gravity *= crits.gravity.getFloatValue();
        particle.hasPhysics = crits.collide.getValue();
        particle.setLifetime(crits.maxAge.getIntValue());

        return particle;
    }
}