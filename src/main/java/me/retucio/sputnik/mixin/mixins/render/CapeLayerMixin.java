package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.player.Capes;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {

    @ModifyExpressionValue(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/PlayerSkin;cape()Lnet/minecraft/core/ClientAsset$Texture;"))
    private ClientAsset.Texture modifyCapeTexture(ClientAsset.Texture original) {
        Capes capes = ModuleManager.INSTANCE.getModuleByClass(Capes.class);
        if (!capes.isEnabled()) return original;

        Identifier id = capes.cape.getValue().getId();
        return id == null ? original : new ClientAsset.ResourceTexture(id, id);
    }
}