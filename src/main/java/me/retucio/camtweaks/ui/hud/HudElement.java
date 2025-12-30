package me.retucio.camtweaks.ui.hud;

import me.retucio.camtweaks.module.modules.HUD;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public abstract class HudElement {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String id;
    protected boolean visible;
    protected int x, y;
    protected final int defaultX, defaultY;
    protected int width = 85, height = 14;

    public HudElement(String id, int defaultX, int defaultY) {
        this.id = id;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.visible = true;
    }

    public abstract String getText(float delta, HUD hud);
    public abstract String getPreviewText();

    public void renderInGame(DrawContext ctx, float delta, HUD hud) {
        String text = getText(delta, hud);
        int color = hud != null ? HudRenderer.getColor(hud).getRGB() : -1;
        boolean shadow = hud != null && hud.shadow.isEnabled();
        HudRenderer.drawSnappedText(ctx, text, x, y, color, shadow);
    }

    public void renderInEditor(DrawContext ctx, HUD hud) {
        String previewText = getPreviewText();
        width = mc.textRenderer.getWidth(previewText);
        height = mc.textRenderer.fontHeight;

        drawEditorBackground(ctx);

        int color = hud != null ? HudRenderer.getColor(hud).getRGB() : -1;
        boolean shadow = hud != null && hud.shadow.isEnabled();
        HudRenderer.drawSnappedText(ctx, previewText, x, y, color, shadow);
    }

    public abstract List<Text> getTooltip();

    private void drawEditorBackground(DrawContext ctx) {
        int bg = visible ? Colors.visibleHudElementColor.getRGB() : Colors.disabledHudElementColor.getRGB();
        int outline = HudEditorScreen.INSTANCE != null && HudEditorScreen.INSTANCE.isSelected(this)
                ? Colors.selectedHudElementOutlineColor.getRGB()
                : Colors.unselectedHudElementOutlineColor.getRGB();

        // fondo
        ctx.fill(x - 1, y - 1, x + width + 1, y + height + 1, bg);

        // contorno
        ctx.fill(x - 1, y - 1, x + width + 1, y, outline);
        ctx.fill(x - 1, y + height, x + width + 1, y + height + 1, outline);
        ctx.fill(x - 1, y, x, y + height, outline);
        ctx.fill(x + width, y, x + width + 1, y + height, outline);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }

    // Getters and setters
    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public void resetPosition() {
        this.x = defaultX;
        this.y = defaultY;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}