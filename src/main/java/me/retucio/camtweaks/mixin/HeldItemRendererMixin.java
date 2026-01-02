package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.event.events.RenderHeldItemEvent;
import me.retucio.camtweaks.event.events.RenderArmEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.player.HandView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.*;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Unique
    HandView handView;

    @Shadow
    private float equipProgressMainHand;

    @Shadow
    private float equipProgressOffHand;

    @Shadow
    private ItemStack mainHand;

    @Shadow
    private ItemStack offHand;

    @Shadow
    protected abstract boolean shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(MinecraftClient client, EntityRenderManager entityRenderDispatcher, ItemModelManager itemModelManager, CallbackInfo ci) {
        handView = ModuleManager.INSTANCE.getModuleByClass(HandView.class);
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V", shift = At.Shift.BEFORE))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci) {
        EVENT_BUS.post(new RenderHeldItemEvent(matrices, hand));
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IFFLnet/minecraft/util/Arm;)V"))
    private void onRenderArm(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light, CallbackInfo ci) {
        EVENT_BUS.post(new RenderArmEvent(matrices, hand));
    }


    @ModifyReturnValue(method = "shouldSkipHandAnimationOnSwap", at = @At("RETURN"))
    private boolean modifySkipSwapAnimation(boolean original) {
        return original || (handView.isEnabled() && handView.skipSwapping.isEnabled());
    }

    @Inject(method = "applyEatOrDrinkTransformation", at = @At(value = "INVOKE", target = "Ljava/lang/Math;pow(DD)D", shift = At.Shift.BEFORE), cancellable = true)
    private void cancelTransformations(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        if (handView.isEnabled() && handView.noFood.isEnabled()) ci.cancel();
    }

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 2), index = 0)
    private float modifyEquipProgressMainhand(float value) {
        if (mc.player == null) return value;
        float progress = mc.player.getAttackCooldownProgress(1);
        float modified = handView.isEnabled() && handView.oldAnimations.isEnabled() ? 1 : (float) Math.pow(progress, 3);

        return (shouldSkipHandAnimationOnSwap(mainHand, mc.player.getMainHandStack()) ? modified : 0) - equipProgressMainHand;
    }

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 3), index = 0)
    private float modifyEquipProgressOffhand(float value) {
        if (mc.player == null) return value;
        return (shouldSkipHandAnimationOnSwap(offHand, mc.player.getOffHandStack()) ? 1 : 0) - equipProgressOffHand;
    }
}