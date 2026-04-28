package me.retucio.sputnik.mixin.mixins.misc;

import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.network.JoinWorldEvent;
import me.retucio.sputnik.event.interact.OpenScreenEvent;
import me.retucio.sputnik.event.ShutdownEvent;
import me.retucio.sputnik.event.TickEvent;
import me.retucio.sputnik.event.interact.UseItemEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.player.FastUse;
import me.retucio.sputnik.util.interfaces.IVec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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

    @Unique
    private boolean freecamSet = false;

    // eventos

    @Shadow @Nullable
    public Screen screen;

    @Shadow
    public abstract @Nullable Entity getCameraEntity();

    @Shadow
    protected abstract void pick(float partialTicks);

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


    // cámara libre

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float partialTicks, CallbackInfo ci) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);

        if ((freecam.isEnabled()) && this.getCameraEntity() != null && !freecamSet) {
            ci.cancel();
            Entity cameraEntity = this.getCameraEntity();

            double x = cameraEntity.getX();
            double y = cameraEntity.getY();
            double z = cameraEntity.getZ();
            double lastX = cameraEntity.xo;
            double lastY = cameraEntity.yo;
            double lastZ = cameraEntity.zo;
            float yaw = cameraEntity.getYRot();
            float pitch = cameraEntity.getXRot();
            float lastYaw = cameraEntity.yRotO;
            float lastPitch = cameraEntity.xRotO;

            ((IVec3) cameraEntity.position()).sputnik$set(
                    freecam.getPos().x,
                    freecam.getPos().y - cameraEntity.getEyeHeight(cameraEntity.getPose()),
                    freecam.getPos().z
            );

            cameraEntity.xo = freecam.getPrevPos().x;
            cameraEntity.yo = freecam.getPrevPos().y - cameraEntity.getEyeHeight(cameraEntity.getPose());
            cameraEntity.zo = freecam.getPrevPos().z;
            cameraEntity.setYRot(freecam.getYaw());
            cameraEntity.setXRot(freecam.getPitch());
            cameraEntity.yRotO = freecam.getPrevYaw();
            cameraEntity.xRotO = freecam.getPrevPitch();

            freecamSet = true;
            pick(partialTicks);
            freecamSet = false;

            ((IVec3) cameraEntity.position()).sputnik$set(x, y, z);
            cameraEntity.xo = lastX;
            cameraEntity.yo = lastY;
            cameraEntity.zo = lastZ;
            cameraEntity.setYRot(yaw);
            cameraEntity.setXRot(pitch);
            cameraEntity.yRotO = lastYaw;
            cameraEntity.xRotO = lastPitch;
        }
    }
}