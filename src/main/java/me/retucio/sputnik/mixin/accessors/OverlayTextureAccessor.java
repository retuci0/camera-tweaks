package me.retucio.sputnik.mixin.accessors;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OverlayTexture.class)
public interface OverlayTextureAccessor {

    @Mutable @Accessor("texture")
    void setTexture(NativeImageBackedTexture texture);
}
