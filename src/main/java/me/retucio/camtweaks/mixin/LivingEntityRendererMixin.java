package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Freecam;
import me.retucio.camtweaks.module.modules.Nametags;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    @Unique
    Nametags nametags;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(EntityRendererFactory.Context ctx, EntityModel<?> model, float shadowRadius, CallbackInfo ci) {
        nametags = ModuleManager.INSTANCE.getModuleByClass(Nametags.class);
    }

    @ModifyExpressionValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
    private Entity hasLabelGetCameraEntityProxy(Entity cameraEntity) {
        return ModuleManager.INSTANCE.getModuleByClass(Freecam.class).isEnabled() ? null : cameraEntity;
    }

    @ModifyExpressionValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSneaky()Z"))
    private boolean renderSneakingPlayerNametags(boolean original) {
        return (nametags.isEnabled() && nametags.alwaysVisible.isEnabled()) || original;
    }

    @ModifyExpressionValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean renderInvisPlayerNametags(boolean original) {
        if (nametags.isEnabled() && nametags.alwaysVisible.isEnabled()) return false;
        else return original;
    }

    @ModifyReturnValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("RETURN"))
    private boolean shouldRenderPlayerNametag(boolean original, @Local(argsOnly = true) T livingEntity) {
        if (nametags.isEnabled() && livingEntity instanceof PlayerEntity) return nametags.entities.isEnabled(EntityType.PLAYER) && original;
        return original;
    }

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("RETURN"), cancellable = true)
    private void renderSelfNametag(T livingEntity, double d, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof PlayerEntity player) {
            if (player == mc.player && nametags.showSelf.isEnabled()) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}
