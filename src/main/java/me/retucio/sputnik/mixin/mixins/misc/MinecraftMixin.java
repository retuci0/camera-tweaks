package me.retucio.sputnik.mixin.mixins.misc;

import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.network.JoinWorldEvent;
import me.retucio.sputnik.event.interact.OpenScreenEvent;
import me.retucio.sputnik.event.ShutdownEvent;
import me.retucio.sputnik.event.TickEvent;
import me.retucio.sputnik.event.interact.UseItemEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.player.FastUse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    private int rightClickDelay;

    @Shadow
    private static Minecraft instance;


    // eventos

    @Shadow @Nullable
    public Screen screen;

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

    @Inject(method = "setLevel", at = @At("HEAD"), cancellable = true)
    private void onJoinWorld(ClientLevel level, CallbackInfo ci) {
        JoinWorldEvent event = Sputnik.EVENT_BUS.post(new JoinWorldEvent(level));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void onUseItem(CallbackInfo ci, @Local(name = "hand") InteractionHand hand) {
        if (Sputnik.mc.player == null) return;
        UseItemEvent event = Sputnik.EVENT_BUS.post(new UseItemEvent(instance.player.getItemInHand(hand), hand));
        if (event.isCancelled()) ci.cancel();
    }

    // telemetría

    @Inject(method = "allowsTelemetry", at = @At("RETURN"), cancellable = true)
    private void disableMicropenisTelemetryShi(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }


    // precoz

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"))
    private void modifyItemUseCooldown(CallbackInfo ci, @Local(name = "heldItem") ItemStack stack) {
        FastUse fastUse = ModuleManager.INSTANCE.getModuleByClass(FastUse.class);
        if (!fastUse.isEnabled()) return;
        rightClickDelay = fastUse.getCooldown(stack);
    }
}