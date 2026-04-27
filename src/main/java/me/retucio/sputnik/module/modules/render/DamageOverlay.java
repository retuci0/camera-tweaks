package me.retucio.sputnik.module.modules.render;

import com.mojang.blaze3d.platform.NativeImage;
import me.retucio.sputnik.mixin.accessors.OverlayTextureAccessor;
import me.retucio.sputnik.mixin.mixins.render.OverlayTextureMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.*;


/** continúa en:
 * @see OverlayTextureMixin
 */

public class DamageOverlay extends Module {

    private final ColorSetting colorSetting = sgGeneral.add(new ColorSetting(
            "color",
            "color",
            Colors.mainColor,
            false
    ));

    private DynamicTexture texture = null;

    public DamageOverlay() {
        super("superposición de daño",
                "modifica el color en el que se renderiza la superposición de recibir daño",
                Category.RENDER);

        colorSetting.onUpdate(v -> reloadOverlayIfReady());
    }

    @Override
    public void onEnable() {
        reloadOverlayIfReady();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        reloadOverlayIfReady();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (colorSetting.isRainbow()) reloadOverlayIfReady();
    }

    private void reloadOverlayIfReady() {
        if (texture != null) reloadOverlay(texture);
    }

    // recargar el overlay (superposición)
    public void reloadOverlay(DynamicTexture texture) {
        if (mc.gameRenderer == null) return;
        if (this.texture == null) this.texture = texture;

        int color = isEnabled()
                ? new Color(
                        colorSetting.getR(),
                        colorSetting.getG(),
                        colorSetting.getB(),
                        255 - colorSetting.getA()
                ).getRGB()
                : new Color(255, 0, 0, 178).getRGB();

        NativeImage image = texture.getPixels();
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 16; x++)
                image.setPixel(x, y, color);

        uploadTexture();
    }

    // resubir las texturas
    private void uploadTexture() {
        ((OverlayTextureAccessor) mc.gameRenderer.overlayTexture())
                .setTexture(this.texture);

        texture.upload();
    }
}
