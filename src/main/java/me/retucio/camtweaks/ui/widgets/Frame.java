package me.retucio.camtweaks.ui.widgets;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Frame<B extends Button> extends Widget  {

    protected String title;

    protected final List<B> buttons = new ArrayList<>();
    protected List<B> visibleButtons = new ArrayList<>();

    protected int renderY;
    protected int totalHeight = 0;

    protected int dragX, dragY;
    protected boolean dragging;

    public Frame(String title, int x, int y, int w, int h) {
        super(x, y, w, h);
        this.title = title;
    }

    public void drawTooltips(DrawContext ctx, int mouseX, int mouseY) {
        for (Button button : visibleButtons) button.drawTooltip(ctx, mouseX, mouseY);
    }

    protected abstract void updateWidth();

    @Override
    // verificar si el puntero del ratón se encuentra encima
    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + w
                && mouseY > renderY && mouseY < renderY + h;
    }

    public void updateRenderY(int scrollOffset) {
        renderY = y - scrollOffset;
    }

    public List<B> getButtons() { return buttons; }
    public List<B> getVisibleButtons() { return visibleButtons; }

    public int getRenderY() { return renderY; }
    public void setRenderY(int renderY) { this.renderY = renderY; }

    public int getTotalHeight() { return totalHeight; }
    public void setTotalHeight(int totalHeight) { this.totalHeight = totalHeight; }
}
