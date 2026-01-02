package me.retucio.camtweaks.module.modules.render;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.ColorSetting;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.awt.*;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.OverlayTextureMixin
 */

public class DamageOverlay extends Module {

    public ColorSetting colorSetting = addSetting(new ColorSetting("color", "color", new Color(255, 0, 0, 77), false));

    private NativeImageBackedTexture texture = null;

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
        if (texture != null && texture.getImage() != null) reloadOverlay(texture);
    }

    // recargar el overlay (superposición)
    public void reloadOverlay(NativeImageBackedTexture texture) {
        if (this.texture == null) this.texture = texture;

        int color = isEnabled()
                ? new Color(
                        colorSetting.getR(),
                        colorSetting.getG(),
                        colorSetting.getB(),
                        255 - colorSetting.getA()
                ).getRGB()
                : new Color(255, 0, 0, 178).getRGB();

        NativeImage image = texture.getImage();
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 16; x++)
                image.setColorArgb(x, y, color);

        uploadTexture();
    }

    // resubir las texturas
    private void uploadTexture() {
//        GpuTextureView overlayTex = RenderSystem.getDevice().getShaderTexture(33985);
//        RenderSystem.setShaderTexture(33985, overlayTex);
//
//        texture.upload();
//
//        GpuTextureView mainTex = RenderSystem.getShaderTexture(33984);
//        RenderSystem.setShaderTexture(33984, mainTex);
    }
}
