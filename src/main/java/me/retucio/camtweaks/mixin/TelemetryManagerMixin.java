package me.retucio.camtweaks.mixin;

import net.minecraft.client.session.telemetry.TelemetryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TelemetryManager.class)
public class TelemetryManagerMixin {

    @Redirect(method = "computeSender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isTelemetryEnabledByApi()Z"))
    private boolean disableTelemetry(@Coerce Object minecraftClient) {
        return false;
    }
}
