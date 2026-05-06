package me.retucio.sputnik.mixin.mixins.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import me.retucio.sputnik.util.render.GlintRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.SequencedMap;

@Mixin(RenderBuffers.class)
public abstract class RenderBuffersMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;immediateWithBuffers(Ljava/util/SequencedMap;Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;)Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;", ordinal = 0))
    private MultiBufferSource.BufferSource injectGlintBuffers(SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers, ByteBufferBuilder sharedBuffer) {
        if (fixedBuffers instanceof Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map) {
            GlintRenderType.addGlintTypes(map);
        }
        return MultiBufferSource.immediateWithBuffers(fixedBuffers, sharedBuffer);
    }
}