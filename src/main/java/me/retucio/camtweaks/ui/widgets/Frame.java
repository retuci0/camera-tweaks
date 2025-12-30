package me.retucio.camtweaks.ui.widgets;

import me.retucio.camtweaks.ui.screen.ClickGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Frame {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public final String title;
    private final List<Button> buttons = new ArrayList<>();
    private List<Button> visibleButtons = new ArrayList<>();

    private int x, y, w, h;
    private int renderY;
    private int dragX, dragY;
    private boolean dragging;
    public int totalHeight = 0;

    public Frame(String title, int x, int y, int w, int h) {
        this.title = title;

        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public abstract void render(DrawContext ctx, int mouseX, int mouseY, float delta);

    public void drawTooltips(DrawContext ctx, int mouseX, int mouseY) {
        for (Button button : visibleButtons) button.drawTooltip(ctx, mouseX, mouseY);
    }

    public abstract void mouseClicked(int mouseX, int mouseY, int button);
    public abstract void mouseDragged(int mouseX, int mouseY);
    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
    }

    public abstract void updateWidth();

    public List<Button> getButtons() {
        return buttons;
    }

    public List<Button> getVisibleButtons() {
        return visibleButtons;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w && mouseY > renderY && mouseY < renderY + h;
    }

    public void updateRenderY(int scrollOffset) {
        renderY = y - scrollOffset;
    }

}
