package me.retucio.camtweaks.module.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.NumberSetting;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.awt.*;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.OverlayTextureMixin
 */

public class DamageOverlay extends Module {

    public NumberSetting red = addSetting(new NumberSetting("rojo", "r o j o", 255, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "v e r d e", 0, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "a z u l", 0, 0, 255, 1));
    public NumberSetting alpha = addSetting(new NumberSetting("alpha", "o p a c i d a d", 77, 0, 255, 1));

    private NativeImageBackedTexture texture = null;

    public DamageOverlay() {
        super("superposición de daño", "modifica el color en el que se renderiza la superposición de recibir daño");

        NumberSetting[] settings = {red, green, blue, alpha};
        for (NumberSetting setting : settings)
            setting.onUpdate(v -> reloadOverlayIfReady());
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

    private void reloadOverlayIfReady() {
        if (texture != null && texture.getImage() != null) reloadOverlay(texture);
    }

    // recargar el overlay (superposición)
    public void reloadOverlay(NativeImageBackedTexture texture) {
        if (this.texture == null) this.texture = texture;

        int color = isEnabled() ?
                new Color(red.getIntValue(), green.getIntValue(), blue.getIntValue(), 255 - alpha.getIntValue()).getRGB()
                : new Color(255, 0, 0, 178).getRGB();

        NativeImage image = texture.getImage();
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 16; x++)
                image.setColorArgb(x, y, color);

        uploadTexture();
    }

    // resubir las texturas
    private void uploadTexture() {
        GpuTextureView overlayTex = RenderSystem.getShaderTexture(33985);
        RenderSystem.setShaderTexture(33985, overlayTex);

        texture.upload();

        GpuTextureView mainTex = RenderSystem.getShaderTexture(33984);
        RenderSystem.setShaderTexture(33984, mainTex);
    }
}
