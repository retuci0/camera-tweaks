package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.JoinWorldEvent;
import me.retucio.camtweaks.event.events.OpenScreenEvent;
import me.retucio.camtweaks.event.events.ShutdownEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import me.retucio.camtweaks.event.events.UseItemEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.player.FastUse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;
import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    private int itemUseCooldown;

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

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onUseItem(CallbackInfo ci, @Local Hand hand) {
        if (mc.player == null) return;
        UseItemEvent event = EVENT_BUS.post(new UseItemEvent(mc.player.getStackInHand(hand), hand));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"))
    private void modifyItemUseCooldown(CallbackInfo ci, @Local ItemStack stack) {
        FastUse fastUse = ModuleManager.INSTANCE.getModuleByClass(FastUse.class);
        if (!fastUse.isEnabled()) return;
        itemUseCooldown = fastUse.getCooldown(stack);
    }

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void onJoinWorld(ClientWorld world, CallbackInfo ci) {
        JoinWorldEvent event = EVENT_BUS.post(new JoinWorldEvent(world));
        if (event.isCancelled()) ci.cancel();
    }
}