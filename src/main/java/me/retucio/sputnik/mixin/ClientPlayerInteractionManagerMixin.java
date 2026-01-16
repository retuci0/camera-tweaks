package me.retucio.sputnik.mixin;

import me.retucio.sputnik.event.events.AttackEntityEvent;
import me.retucio.sputnik.event.events.BreakBlockEvent;
import me.retucio.sputnik.event.events.InteractEntityEvent;
import me.retucio.sputnik.event.events.PlaceBlockEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.player.FastUse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;
import static me.retucio.sputnik.Sputnik.mc;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    private int blockBreakingCooldown;

    @Unique
    Freecam freecam;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(MinecraftClient client, ClientPlayNetworkHandler networkHandler, CallbackInfo ci) {
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
    }


    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private void onBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BreakBlockEvent event = EVENT_BUS.post(new BreakBlockEvent(pos));
        if (event.isCancelled()) cir.cancel();
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void onBlockPlace(ClientPlayerEntity player, Hand hand, BlockHitResult result, CallbackInfoReturnable<ActionResult> cir) {
        if (mc.player != player) return;
        PlaceBlockEvent event = EVENT_BUS.post(new PlaceBlockEvent(hand, result));
        if (event.isCancelled()) cir.cancel();
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void onEntityInteract(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (mc.player != player) return;
        InteractEntityEvent event = EVENT_BUS.post(new InteractEntityEvent(entity, hand));
        if (event.isCancelled()) cir.cancel();
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (mc.player != player) return;
        AttackEntityEvent event = EVENT_BUS.post(new AttackEntityEvent(target));
        if (event.isCancelled()) ci.cancel();
    }



    @Inject(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I"))
    private void modifyBlockBreakingCooldown(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        FastUse fastUse = ModuleManager.INSTANCE.getModuleByClass(FastUse.class);
        if (!fastUse.isEnabled() || !fastUse.mining.getValue()) return;
        blockBreakingCooldown = fastUse.miningCooldown.getIntValue();
    }
}
