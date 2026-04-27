package me.retucio.sputnik.mixin.mixins.entity;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.interact.BoatMoveEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.movement.BoatFly;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoat.class)
public abstract class AbstractBoatMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void onMove(AbstractBoat instance, MoverType moverType, Vec3 vec3) {
        BoatMoveEvent event = Sputnik.EVENT_BUS.post(new BoatMoveEvent(instance, vec3));
        if (!event.isCancelled()) instance.move(moverType, event.getPos());
    }

    @Inject(method = "controlBoat", at = @At("HEAD"), cancellable = true)
    private void onPaddle(CallbackInfo ci) {
        if (ModuleManager.INSTANCE.getModuleByClass(BoatFly.class).isEnabled()) {
            ci.cancel();
        }
    }
}
