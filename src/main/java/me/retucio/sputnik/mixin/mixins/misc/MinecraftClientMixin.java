package me.retucio.sputnik.mixin.mixins.misc;

import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.events.network.JoinWorldEvent;
import me.retucio.sputnik.event.events.interact.OpenScreenEvent;
import me.retucio.sputnik.event.events.ShutdownEvent;
import me.retucio.sputnik.event.events.TickEvent;
import me.retucio.sputnik.event.events.interact.UseItemEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.player.FastUse;
import me.retucio.sputnik.ui.screen.UpdateScreen;
import me.retucio.sputnik.util.VersionChecker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    private int itemUseCooldown;

    @Shadow
    public int attackCooldown;


    // eventos

    @Shadow
    @Nullable
    public Screen currentScreen;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        Sputnik.INSTANCE.onTick();
        Sputnik.EVENT_BUS.post(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickPost(CallbackInfo ci) {
        Sputnik.EVENT_BUS.post(new TickEvent.Post());
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        Sputnik.EVENT_BUS.post(new ShutdownEvent());
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onOpenScreen(Screen screen, CallbackInfo ci) {
        OpenScreenEvent event = Sputnik.EVENT_BUS.post(new OpenScreenEvent(screen));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void onJoinWorld(ClientWorld world, CallbackInfo ci) {
        JoinWorldEvent event = Sputnik.EVENT_BUS.post(new JoinWorldEvent(world));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onUseItem(CallbackInfo ci, @Local Hand hand) {
        if (Sputnik.mc.player == null) return;
        UseItemEvent event = Sputnik.EVENT_BUS.post(new UseItemEvent(Sputnik.mc.player.getStackInHand(hand), hand));
        if (event.isCancelled()) ci.cancel();
    }

    // telemetría

    @Inject(method = "isTelemetryEnabledByApi", at = @At("RETURN"), cancellable = true)
    private void disableMicropenisTelemetryShi(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }


    // precoz

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"))
    private void modifyItemUseCooldown(CallbackInfo ci, @Local ItemStack stack) {
        FastUse fastUse = ModuleManager.INSTANCE.getModuleByClass(FastUse.class);
        if (!fastUse.isEnabled()) return;
        itemUseCooldown = fastUse.getCooldown(stack);
    }
}