package me.retucio.sputnik.mixin.mixins.render;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.Hitboxes;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {

    @ModifyVariable(method = "showHitboxes", at = @At("STORE"), name = "mainColor")
    private int modifyHitboxColor(int original) {
        Hitboxes hitboxes = ModuleManager.INSTANCE.getModuleByClass(Hitboxes.class);
        if (hitboxes.isEnabled()) return hitboxes.color.getRGB();
        return original;
    }

    @ModifyArg(method = "showHitboxes", at = @At(value = "INVOKE", target = "Lnet/minecraft/gizmos/Gizmos;cuboid(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/gizmos/GizmoStyle;)Lnet/minecraft/gizmos/GizmoProperties;"))
    private GizmoStyle modifyHitboxLineWidth(GizmoStyle original) {
        Hitboxes hitboxes = ModuleManager.INSTANCE.getModuleByClass(Hitboxes.class);
        if (hitboxes.isEnabled()) return GizmoStyle.stroke(original.stroke(), hitboxes.lineWidth.getFloatValue());
        return original;
    }
}
