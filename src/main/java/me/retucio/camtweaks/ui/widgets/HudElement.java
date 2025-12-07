package me.retucio.camtweaks.ui.widgets;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.HUD;
import me.retucio.camtweaks.ui.HudEditorScreen;
import me.retucio.camtweaks.ui.HudRenderer;
import me.retucio.camtweaks.util.Colors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class HudElement {

    protected final MinecraftClient mc = MinecraftClient.getInstance();

    private final String id;
    protected boolean visible;

    protected int x, y;
    protected int defaultX, defaultY;
    protected int width = 85, height = 14;  // tamaño por defecto

    public HudElement(String id, int defX, int defY, int x, int y, boolean visible) {
        this.id = id;
        this.visible = visible;
        this.x = x;
        this.y = y;
        this.defaultX = defX;
        this.defaultY = defY;
    }

    // fallback
    public void render(DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        ctx.drawText(mc.textRenderer, Text.literal(getId()), x, y, -1, false);
    }

    /** renderizar previsualización del elemento para el editor */
    public void renderPreview(DrawContext ctx) {
        final HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        final String previewText = buildPreviewText(hud);

        width = mc.textRenderer.getWidth(previewText);
        height = mc.textRenderer.fontHeight;

        drawBackground(ctx);

        int color = -1;
        boolean shadow = false;
        if (hud != null) {
            color = HudRenderer.getColor(hud).getRGB();
            shadow = hud.shadow.isEnabled();
        }

        HudRenderer.drawSnappedText(ctx, previewText, x, y, color, shadow);
    }

    /** construir texto para la previsualización */
    private String buildPreviewText(HUD hud) {
        float delta = mc.getRenderTickCounter().getDynamicDeltaTicks();

        if ("coords".equals(id)) {
            if (mc.player == null) return "X Y Z";
            return HudRenderer.getElementText(id, delta, hud);
        }

        if ("fps".equals(id)) {
            return HudRenderer.getElementText(id, delta, hud);
        }

        if ("tps".equals(id)) {
            return HudRenderer.getElementText(id, delta, hud);
        }

        if ("customText".equals(id)) {
            if (hud != null && hud.customText != null && !hud.customText.getValue().isEmpty())
                return HudRenderer.getElementText(id, delta, hud);
            return "texto custom";
        }

        if ("time".equals(id)) {
            if (hud != null && hud.timeFormat != null)
                return HudRenderer.getElementText(id, delta, hud);
            return "4:20 PM";
        }

        return id;  // fallback: id del elemento
    }

    /** fondo y contorno */
    private void drawBackground(DrawContext ctx) {
        int bg = visible ? Colors.visibleHudElementColor.getRGB() : Colors.disabledHudElementColor.getRGB();
        int outline = HudEditorScreen.INSTANCE.isSelected(this) ? Colors.selectedHudElementOutlineColor.getRGB() : Colors.unselectedHudElementOutlineColor.getRGB();

        // fondo
        ctx.fill(x - 1, y - 1, x + width + 1, y + height + 1, bg);

        // contorno
        ctx.fill(x - 1, y - 1, x + width + 1, y, outline);
        ctx.fill(x - 1, y + height, x + width + 1, y + height + 1, outline);
        ctx.fill(x - 1, y, x, y + height, outline);
        ctx.fill(x + width, y, x + width + 1, y + height, outline);
    }

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y  && mouseY <= y + height;
    }

    // getters y setters
    public String getId() { return id; }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public void resetPosition() {
        this.x = defaultX;
        this.y = defaultY;
    }

    public interface Renderer {
        void render(DrawContext ctx, int x, int y, float delta);
    }
}
