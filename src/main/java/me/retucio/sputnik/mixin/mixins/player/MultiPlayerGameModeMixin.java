package me.retucio.sputnik.mixin.mixins.player;

import me.retucio.sputnik.event.interact.*;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.player.FastUse;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;
import static me.retucio.sputnik.Sputnik.mc;


@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Shadow
    private int destroyDelay;

    @Unique Freecam freecam;
    @Unique FastUse fastUse;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        fastUse = ModuleManager.INSTANCE.getModuleByClass(FastUse.class);
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void onBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BreakBlockEvent event = EVENT_BUS.post(new BreakBlockEvent(pos));
        if (event.isCancelled()) cir.cancel();
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onBlockPlace(LocalPlayer player, InteractionHand hand, BlockHitResult blockHit, CallbackInfoReturnable<InteractionResult> cir) {
        if (mc.player != player) return;
        PlaceBlockEvent event = EVENT_BUS.post(new PlaceBlockEvent(hand, blockHit));
        if (event.isCancelled()) cir.cancel();
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        AttackBlockEvent event = EVENT_BUS.post(new AttackBlockEvent(pos, direction));
        if (event.isCancelled()) cir.cancel();
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onEntityInteract(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (mc.player != player) return;
        InteractEntityEvent event = EVENT_BUS.post(new InteractEntityEvent(entity, hand));
        if (event.isCancelled()) cir.cancel();
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(Player player, Entity entity, CallbackInfo ci) {
        if (mc.player != player) return;
        AttackEntityEvent event = EVENT_BUS.post(new AttackEntityEvent(entity));
        if (event.isCancelled()) ci.cancel();
    }


    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void modifyBlockBreakingCooldown(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!fastUse.isEnabled() || !fastUse.mining.getValue()) return;
        destroyDelay = fastUse.miningCooldown.getIntValue();
    }
}
