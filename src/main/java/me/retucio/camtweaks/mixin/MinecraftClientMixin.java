package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.OpenScreenEvent;
import me.retucio.camtweaks.event.events.ShutdownEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        CameraTweaks.INSTANCE.onTick();
        EVENT_BUS.post(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickPost(CallbackInfo ci) {
        EVENT_BUS.post(new TickEvent.Post());
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        EVENT_BUS.post(new ShutdownEvent());
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        OpenScreenEvent event = EVENT_BUS.post(new OpenScreenEvent(screen));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "isTelemetryEnabledByApi", at = @At("RETURN"), cancellable = true)
    private void disableMicropenisTelemetryShi(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}