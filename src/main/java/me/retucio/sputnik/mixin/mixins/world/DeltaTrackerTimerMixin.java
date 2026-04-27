package me.retucio.sputnik.mixin.mixins.world;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.world.Timer;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
public abstract class DeltaTrackerTimerMixin {

    @Shadow
    private float deltaTicks;

    @Inject(method = "advanceGameTime(J)I", at = @At(value = "FIELD", target = "Lnet/minecraft/client/DeltaTracker$Timer;lastMs:J", opcode = Opcodes.PUTFIELD))
    private void onBeingRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        deltaTicks *= ModuleManager.INSTANCE.getModuleByClass(Timer.class).multiplier.getFloatValue();
    }
}
