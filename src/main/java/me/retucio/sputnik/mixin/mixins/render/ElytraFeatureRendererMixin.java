package me.retucio.sputnik.mixin.mixins.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.player.Capes;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin<S extends BipedEntityRenderState, M extends EntityModel<S>> extends FeatureRenderer<S, M> {

    public ElytraFeatureRendererMixin(FeatureRendererContext<S, M> context) {
        super(context);
    }

    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ElytraFeatureRenderer;getTexture(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)Lnet/minecraft/util/Identifier;"))
    private Identifier modifyCapeTexture(Identifier original, MatrixStack matrices, OrderedRenderCommandQueue entityRenderCommandQueue, int i, S state, float f, float g) {
        Capes capes = ModuleManager.INSTANCE.getModuleByClass(Capes.class);
        if (!capes.isEnabled()) return original;

        Identifier id = capes.cape.getValue().getId();
        return id == null ? original : id;
    }
}