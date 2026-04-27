package me.retucio.sputnik.ui.hud;

import com.mojang.blaze3d.platform.NativeImage;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;


import java.io.InputStream;
import java.util.function.Supplier;


public abstract class ImageHudElement extends HudElement {

    protected Identifier textureId;
    protected int imageWidth = 64;
    protected int imageHeight = 64;
    protected boolean textureLoaded = false;
    protected DynamicTexture texture;

    public ImageHudElement(String id, int defaultX, int defaultY) {
        super(id, defaultX, defaultY);
        this.textureId = Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "textures/" + id);
        this.w = imageWidth;
        this.h = imageHeight;
    }

    protected abstract String getImagePath();

    public void reloadTexture() {
        textureLoaded = false;
        String imagePath = getImagePath();
        if (imagePath == null || imagePath.isEmpty()) return;

        try {
            // cargar
            Identifier resourceId = Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, imagePath);
            try (InputStream stream = mc.getResourceManager().getResource(resourceId).get().open()) {
                NativeImage image = NativeImage.read(stream);

                // deshacerse de la textura vieja
                if (texture != null) texture.close();
                if (mc.getTextureManager().getTexture(textureId) != null) {
                    mc.getTextureManager().release(textureId);
                }

                // crear la textura nueva
                Supplier<String> nameSupplier = () -> textureId.toString();
                texture = new DynamicTexture(nameSupplier, image);

                this.imageWidth = image.getWidth();
                this.imageHeight = image.getHeight();
                this.w = imageWidth;
                this.h = imageHeight;

                mc.getTextureManager().register(textureId, texture);
                textureLoaded = true;

                Sputnik.LOGGER.info("textura para el elemento del HUD {} cargada", getId());
            }
        } catch (Exception e) {
            Sputnik.LOGGER.error("no se pudo cargar la textura para el elemento del HUD {}: {}", getId(), e.getMessage());
        }
    }

    @Override
    public void renderInGame(GuiGraphicsExtractor gui, float delta, HUD hud) {
        if (!textureLoaded || !isVisible()) return;

        gui.blit(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                x, y,
                0, 0,
                imageWidth, imageHeight,
                imageWidth, imageHeight
        );
    }

    @Override
    public void renderInEditor(GuiGraphicsExtractor gui, HUD hud) {
        w = imageWidth;
        h = imageHeight;

        drawEditorBackground(gui);

        if (textureLoaded) {
            gui.blit(
                    RenderPipelines.GUI_TEXTURED,
                    textureId,
                    x, y,
                    0, 0,
                    imageWidth, imageHeight,
                    imageWidth, imageHeight
            );
        } else {
            String placeholder = "imagen: " + getId();
            int textWidth = mc.font.width(placeholder);
            int textX = x + Math.max(0, (w - textWidth) / 2);
            int textY = y + Math.max(0, (h - mc.font.lineHeight) / 2);
            gui.text(mc.font, placeholder, textX, textY, -1, true);
        }
    }

    protected void drawEditorBackground(GuiGraphicsExtractor gui) {
        int bgColor = visible ? Colors.visibleHudElementColor.getRGB() : Colors.disabledHudElementColor.getRGB();
        int outlineColor = HudEditorScreen.INSTANCE != null && HudEditorScreen.INSTANCE.isSelected(this)
                ? Colors.selectedHudElementOutlineColor.getRGB()
                : Colors.unselectedHudElementOutlineColor.getRGB();

        // fondo
        gui.fill(x - 1, y - 1, x + w + 1, y + h + 1, bgColor);

        // contorno
        gui.fill(x - 1, y - 1, x + w + 1, y, outlineColor);
        gui.fill(x - 1, y + h, x + w + 1, y + h + 1, outlineColor);
        gui.fill(x - 1, y, x, y + h, outlineColor);
        gui.fill(x + w, y, x + w + 1, y + h, outlineColor);
    }
}