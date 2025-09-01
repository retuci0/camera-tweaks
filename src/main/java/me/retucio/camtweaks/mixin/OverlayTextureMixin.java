package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.DamageOverlay;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlayTexture.class)
public abstract class OverlayTextureMixin implements AutoCloseable {

    @Shadow @Final
    private NativeImageBackedTexture texture;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void modifyDamageOverlayColor(CallbackInfo ci) {
        ModuleManager.INSTANCE.getModuleByClass(DamageOverlay.class).reloadOverlay(texture);
    }
}