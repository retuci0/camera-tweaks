package me.retucio.sputnik.mixin.mixins.world;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.CritsPlus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TrackingEmitter.class)
public abstract class TrackingEmitterMixin extends NoRenderParticle {

    @Final @Shadow private Entity entity;
    @Final @Shadow private ParticleOptions particleType;
    @Final @Shadow private int lifeTime;
    @Shadow private int life;

    protected TrackingEmitterMixin(ClientLevel clientWorld, double d, double e, double f) {
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
                ? critsPlus.multiplier.getValue() : 1
        );

        for (int i = 0; i < amount; i++) {
            double xa = this.random.nextFloat() * 2.0F - 1.0F;
            double ya = this.random.nextFloat() * 2.0F - 1.0F;
            double za = this.random.nextFloat() * 2.0F - 1.0F;
            if (!(xa * xa + ya * ya + za * za > (double)1.0F)) {
                double x = this.entity.getX(xa / (double)4.0F);
                double y = this.entity.getY((double)0.5F + ya / (double)4.0F);
                double z = this.entity.getZ(za / (double)4.0F);
                this.level.addParticle(this.particleType, x, y, z, xa, ya + 0.2, za);
            }
        }

        this.life++;
        if (this.life >= this.lifeTime) {
            this.remove();
        }
    }
}
