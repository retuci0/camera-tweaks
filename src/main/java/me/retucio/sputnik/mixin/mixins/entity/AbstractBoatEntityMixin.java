package me.retucio.sputnik.mixin.mixins.entity;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.interact.BoatMoveEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.movement.BoatFly;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntity.class)
public abstract class AbstractBoatEntityMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/AbstractBoatEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    private void onMove(AbstractBoatEntity instance, MovementType movementType, Vec3d vec3d) {
        BoatMoveEvent event = Sputnik.EVENT_BUS.post(new BoatMoveEvent(instance, vec3d));
        if(!event.isCancelled()) instance.move(movementType, event.getPos());
    }

    @Inject(method = "updatePaddles", at = @At("HEAD"), cancellable = true)
    private void onPaddle(CallbackInfo ci) {
        if (ModuleManager.INSTANCE.getModuleByClass(BoatFly.class).isEnabled()) {
            ci.cancel();
        }
    }
}
