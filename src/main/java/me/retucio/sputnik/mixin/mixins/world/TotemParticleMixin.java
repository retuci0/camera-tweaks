package me.retucio.sputnik.mixin.mixins.world;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.Confetti;
import net.minecraft.client.particle.TotemParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TotemParticle.class)
public abstract class TotemParticleMixin {

    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/TotemParticle;setColor(FFF)V", ordinal = 0))
    private void setConfettiColor1(Args args) {
        Confetti confetti = ModuleManager.INSTANCE.getModuleByClass(Confetti.class);
        if (confetti.isEnabled()) {
            args.set(0, confetti.color1.getR() / 255f);
            args.set(1, confetti.color1.getG() / 255f);
            args.set(2, confetti.color1.getB() / 255f);
        }
    }

    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/TotemParticle;setColor(FFF)V", ordinal = 1))
    private void setConfettiColor2(Args args) {
        Confetti confetti = ModuleManager.INSTANCE.getModuleByClass(Confetti.class);
        if (confetti.isEnabled()) {
            args.set(0, confetti.color2.getR() / 255f);
            args.set(1, confetti.color2.getG() / 255f);
            args.set(2, confetti.color2.getB() / 255f);
        }
    }
}
