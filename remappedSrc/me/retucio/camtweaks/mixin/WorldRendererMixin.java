package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.BlockOutline;
import me.retucio.camtweaks.module.modules.Freecam;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean renderSetupTerrainModifyArg(boolean hasForcedFrustum) {
        return ModuleManager.INSTANCE.getModuleByClass(Freecam.class).isEnabled() || hasForcedFrustum;
    }

    @Redirect(method = "renderTargetBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ColorHelper;withAlpha(II)I"))
    private int modifyBlockOutlineColor(int alpha, int rgb) {
        BlockOutline outline = ModuleManager.INSTANCE.getModuleByClass(BlockOutline.class);
        if (!outline.isEnabled()) return ColorHelper.withAlpha(alpha, rgb);

        if (outline.rainbow.isEnabled()) {
            float speed = 10001 - outline.rainbowSpeed.getFloatValue();  // 10001 para evitar divisiones por cero
            float hue = (System.currentTimeMillis() % (int) speed) / speed;
            Color gamingProMax = Color.getHSBColor(hue, 1, 1);

            return new Color(
                    gamingProMax.getRed(),
                    gamingProMax.getGreen(),
                    gamingProMax.getBlue(),
                    outline.alpha.getIntValue()
            ).getRGB();
        } else {
            return new Color(
                    outline.red.getIntValue(),
                    outline.green.getIntValue(),
                    outline.blue.getIntValue(),
                    outline.alpha.getIntValue()
            ).getRGB();
        }
    }
}