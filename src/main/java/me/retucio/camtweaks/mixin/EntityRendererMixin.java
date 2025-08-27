package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.mixin.accessor.EntityRenderStateAccessor;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Nametags;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends EntityRenderState> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void renderSneakingPlayerNametags(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        Nametags nametags = ModuleManager.INSTANCE.getModuleByClass(Nametags.class);

        if (nametags.isEnabled() && nametags.alwaysVisible.isEnabled())
            ((EntityRenderStateAccessor) state).setSneaking(false);
    }
}