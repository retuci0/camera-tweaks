package me.retucio.sputnik.mixin.mixins.world;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.CritsPlus;
import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EmitterParticle.class)
public abstract class EmitterParticleMixin extends NoRenderParticle {

    @Final @Shadow private Entity entity;
    @Final @Shadow private ParticleEffect parameters;
    @Final @Shadow private int maxEmitterAge;
    @Shadow private int emitterAge;

    protected EmitterParticleMixin(ClientWorld clientWorld, double d, double e, double f) {
        super(clientWorld, d, e, f);
    }

    /**
     * @author retucio
     * @reason porque no hay otra manera más fácil
     */
    @Overwrite
    public void tick() {
        CritsPlus critsPlus = ModuleManager.INSTANCE.getModuleByClass(CritsPlus.class);

        double amount = 16 * (critsPlus.isEnabled()
                ? critsPlus.multiplier.getValue()
                : 1);

        for (int i = 0; i < amount; i++) {
            double d = this.random.nextFloat() * 2.0F - 1.0F;
            double e = this.random.nextFloat() * 2.0F - 1.0F;
            double f = this.random.nextFloat() * 2.0F - 1.0F;
            if (!(d * d + e * e + f * f > 1.0)) {
                double g = this.entity.getBodyX(d / 4.0);
                double h = this.entity.getBodyY(0.5 + e / 4.0);
                double j = this.entity.getBodyZ(f / 4.0);
                this.world.addParticleClient(this.parameters, g, h, j, d, e + 0.2, f);
            }
        }

        this.emitterAge++;
        if (this.emitterAge >= this.maxEmitterAge) {
            this.markDead();
        }
    }
}
