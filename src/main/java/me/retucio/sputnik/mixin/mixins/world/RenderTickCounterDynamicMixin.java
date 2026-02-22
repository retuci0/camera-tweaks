package me.retucio.sputnik.mixin.mixins.world;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.world.Timer;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public abstract class RenderTickCounterDynamicMixin {

    @Shadow
    private float dynamicDeltaTicks;

    @Inject(method = "beginRenderTick(J)I", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;lastTimeMillis:J", opcode = Opcodes.PUTFIELD))
    private void onBeingRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        dynamicDeltaTicks *= ModuleManager.INSTANCE.getModuleByClass(Timer.class).multiplier.getFloatValue();
    }
}
