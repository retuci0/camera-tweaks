package me.retucio.sputnik.mixin.mixins.render;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.DamageOverlay;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlayTexture.class)
public abstract class OverlayTextureMixin implements AutoCloseable {

    @Shadow @Final
    private DynamicTexture texture;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void modifyDamageOverlayColor(CallbackInfo ci) {
        ModuleManager.INSTANCE.getModuleByClass(DamageOverlay.class).reloadOverlay(texture);
    }
}