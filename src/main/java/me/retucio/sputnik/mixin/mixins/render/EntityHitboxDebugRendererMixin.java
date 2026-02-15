package me.retucio.sputnik.mixin.mixins.render;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.Hitboxes;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {

    @ModifyVariable(method = "drawHitbox", at = @At("STORE"))
    private int modifyHitboxColor(int original) {
        Hitboxes hitboxes = ModuleManager.INSTANCE.getModuleByClass(Hitboxes.class);
        if (hitboxes.isEnabled()) return hitboxes.color.getRGB();
        return original;
    }

    @ModifyArg(method = "drawHitbox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/debug/gizmo/GizmoDrawing;box(Lnet/minecraft/util/math/Box;Lnet/minecraft/client/render/DrawStyle;)Lnet/minecraft/world/debug/gizmo/VisibilityConfigurable;"))
    private DrawStyle modifyHitboxLineWidth(DrawStyle original) {
        Hitboxes hitboxes = ModuleManager.INSTANCE.getModuleByClass(Hitboxes.class);
        if (hitboxes.isEnabled()) return DrawStyle.stroked(original.stroke(), hitboxes.lineWidth.getFloatValue());
        return original;
    }
}
