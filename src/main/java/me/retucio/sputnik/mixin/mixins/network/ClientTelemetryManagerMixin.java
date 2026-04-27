package me.retucio.sputnik.mixin.mixins.network;

import com.mojang.authlib.minecraft.TelemetrySession;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(ClientTelemetryManager.class)
public class ClientTelemetryManagerMixin {

    @Redirect(method = "createEventSender", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/TelemetrySession;isEnabled()Z"))
    private boolean disableTelemetry(TelemetrySession instance) {
        return false;
    }
}
