package me.retucio.camtweaks.mixin.accessor;

import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderState.class)
public interface EntityRenderStateAccessor {

    @Accessor("sneaking")
    void setSneaking(boolean sneaking);
}
