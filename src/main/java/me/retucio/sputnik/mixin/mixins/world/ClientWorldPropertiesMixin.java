package me.retucio.sputnik.mixin.mixins.world;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.world.TimeChanger;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.Properties.class)
public abstract class ClientWorldPropertiesMixin {

    @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void setTimeOfDay(long time, CallbackInfo ci) {  // de no ser por esto, se verían "flashes" del tiempo real en el cielo
        TimeChanger timeChanger = ModuleManager.INSTANCE.getModuleByClass(TimeChanger.class);
        if (timeChanger.isEnabled() && time != timeChanger.time.getLongValue()) ci.cancel();
    }
}
