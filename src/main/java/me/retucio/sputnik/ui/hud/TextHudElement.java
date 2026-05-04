package me.retucio.sputnik.ui.hud;

import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;


public abstract class TextHudElement extends HudElement {

    public TextHudElement(String id, int defaultX, int defaultY) {
        super(id, defaultX, defaultY);
    }

    public abstract String getText(float delta, Hud hud);
    public abstract String getPreviewText();

    @Override
    public void renderInGame(GuiGraphicsExtractor gui, float delta, Hud hud) {
        String text = getText(delta, hud);
        int color = hud != null ? hud.color.getValue().getRGB() : -1;
        boolean shadow = hud != null && hud.shadow.getValue();
        HudRenderer.drawSnappedText(gui, text, x, y, color, shadow);
    }

    @Override
    public void renderInEditor(GuiGraphicsExtractor gui, Hud hud) {
        String previewText = getPreviewText();
        w = mc.font.width(previewText);
        h = mc.font.lineHeight;

        drawEditorBackground(gui);

        int color = hud != null ? hud.color.getValue().getRGB() : -1;
        boolean shadow = hud != null && hud.shadow.getValue();
        HudRenderer.drawSnappedText(gui, previewText, x, y, color, shadow);
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