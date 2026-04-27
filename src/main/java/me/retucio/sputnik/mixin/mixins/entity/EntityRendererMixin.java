package me.retucio.sputnik.mixin.mixins.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.EntityESP;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Unique
    private EntityESP esp;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        esp = ModuleManager.INSTANCE.getModuleByClass(EntityESP.class);
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean renderGlowESP(boolean original, @Local(argsOnly = true) Entity entity) {
        return (esp.isEnabled() && esp.mode.is(EntityESP.ESPMode.GLOW) && esp.entities.isEnabled(entity.getType())) || original;
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
    private int setGlowESPColor(int original, @Local(argsOnly = true) Entity entity) {
        if (esp.isEnabled() && esp.mode.is(EntityESP.ESPMode.GLOW) && esp.entities.isEnabled(entity.getType())) return esp.glowColor.getRGB();
        return original;
    }
}
