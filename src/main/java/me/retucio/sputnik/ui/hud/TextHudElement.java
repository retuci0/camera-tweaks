package me.retucio.sputnik.ui.hud;

import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.DrawContext;


public abstract class TextHudElement extends HudElement {

    public TextHudElement(String id, int defaultX, int defaultY) {
        super(id, defaultX, defaultY);
    }

    public abstract String getText(float delta, HUD hud);
    public abstract String getPreviewText();

    @Override
    public void renderInGame(DrawContext ctx, float delta, HUD hud) {
        String text = getText(delta, hud);
        int color = hud != null ? hud.color.getValue().getRGB() : -1;
        boolean shadow = hud != null && hud.shadow.getValue();
        HudRenderer.drawSnappedText(ctx, text, x, y, color, shadow);
    }

    @Override
    public void renderInEditor(DrawContext ctx, HUD hud) {
        String previewText = getPreviewText();
        w = mc.textRenderer.getWidth(previewText);
        h = mc.textRenderer.fontHeight;

        drawEditorBackground(ctx);

        int color = hud != null ? hud.color.getValue().getRGB() : -1;
        boolean shadow = hud != null && hud.shadow.getValue();
        HudRenderer.drawSnappedText(ctx, previewText, x, y, color, shadow);
    }

    protected void drawEditorBackground(DrawContext ctx) {
        int bgColor = visible ? Colors.visibleHudElementColor.getRGB() : Colors.disabledHudElementColor.getRGB();
        int outlineColor = HudEditorScreen.INSTANCE != null && HudEditorScreen.INSTANCE.isSelected(this)
                ? Colors.selectedHudElementOutlineColor.getRGB()
                : Colors.unselectedHudElementOutlineColor.getRGB();

        // fondo
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, bgColor);

        // contorno
        ctx.fill(x - 1, y - 1, x + w + 1, y, outlineColor);
        ctx.fill(x - 1, y + h, x + w + 1, y + h + 1, outlineColor);
        ctx.fill(x - 1, y, x, y + h, outlineColor);
        ctx.fill(x + w, y, x + w + 1, y + h, outlineColor);
    }
}