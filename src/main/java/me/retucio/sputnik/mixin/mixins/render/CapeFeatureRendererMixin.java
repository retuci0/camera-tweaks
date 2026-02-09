package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.player.Capes;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {

    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/SkinTextures;cape()Lnet/minecraft/util/AssetInfo$TextureAsset;"))
    private AssetInfo.TextureAsset modifyCapeTexture(AssetInfo.TextureAsset original, MatrixStack matrices, OrderedRenderCommandQueue entityRenderCommandQueue, int i, PlayerEntityRenderState state, float f, float g) {
        Capes capes = ModuleManager.INSTANCE.getModuleByClass(Capes.class);
        if (!capes.isEnabled()) return original;

        Identifier id = capes.cape.getValue().getId();
        return id == null ? original : new AssetInfo.TextureAssetInfo(id, id);
    }
}