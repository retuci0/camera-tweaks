package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Freecam;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Unique
    Freecam freecam;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(MinecraftClient client, ClientPlayNetworkHandler networkHandler, CallbackInfo ci) {
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
    }

    // los paquetes ya se cancelan en la clase de Freecam, pero visualmente el bloque sigue apareciendo roto
    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private void cancelBlockBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (freecam.isEnabled() && freecam.cancelActionPackets.isEnabled()) cir.setReturnValue(false);
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void cancelBlockPlacement(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (freecam.isEnabled() && freecam.cancelActionPackets.isEnabled()) cir.setReturnValue(ActionResult.FAIL);
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void cancelEntityInteractions(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (freecam.isEnabled() && freecam.cancelActionPackets.isEnabled()) cir.setReturnValue(ActionResult.FAIL);
    }
}
