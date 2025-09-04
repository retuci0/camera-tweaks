package me.retucio.camtweaks.ui.widgets;

import me.retucio.camtweaks.ui.ClickGUI;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class ScrollBarWidget {

    private boolean dragging;
    private int dragY, scrollStart;
    private int scrollOffset = 0;

    private int contentHeight, windowHeight;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public ScrollBarWidget() {}

    public void render(DrawContext ctx, double mouseX, double mouseY) {
        if (contentHeight <= windowHeight || !ClientSettingsFrame.guiSettings.scrollBar.isEnabled()) return;

        int trackX1 = mc.getWindow().getScaledWidth() - 10;
        int trackX2 = trackX1 + 10;

        ctx.fill(trackX1, 0, trackX2, windowHeight, Colors.buttonColor.getRGB());

        int thumbHeight = getThumbHeight();
        int thumbY = getThumbY();

        Color thumbColor = isThumbHovered(mouseX, mouseY)
                ? Colors.mainColor.brighter()
                : Colors.mainColor;

        if (dragging) thumbColor = Colors.mainColor.darker();

        ctx.fill(trackX1 + 1, thumbY + 1, trackX2 - 1, thumbY + thumbHeight - 1, thumbColor.getRGB());
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !ClientSettingsFrame.guiSettings.scrollBar.isEnabled()) return;
        if (isThumbHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            dragging = true;
            dragY = (int) mouseY;
            scrollStart = scrollOffset;
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
        dragging = false;
    }

    public void mouseDragged(double mouseY) {
        if (!dragging || !ClientSettingsFrame.guiSettings.scrollBar.isEnabled()) return;

        int thumbHeight = getThumbHeight();
        int trackHeight = windowHeight - thumbHeight;

        float ratio = (float) (contentHeight - windowHeight) / trackHeight;
        int deltaY = (int) mouseY - dragY;
        scrollOffset = scrollStart + (int) (deltaY * ratio);
        clampOffset();
    }

    public void onMouseScroll(double amount) {
        if (contentHeight <= windowHeight) return;
        scrollOffset -= (int) amount * 20;
        clampOffset();
    }

    private boolean isThumbHovered(double mouseX, double mouseY) {
        if (!ClickGUI.INSTANCE.canSelect(this)) return false;
        int trackX1 = mc.getWindow().getScaledWidth() - 10;
        int trackX2 = trackX1 + 10;
        int thumbY = getThumbY();
        int thumbHeight = getThumbHeight();
        return mouseX >= trackX1 && mouseX <= trackX2 && mouseY >= thumbY && mouseY <= thumbY + thumbHeight;
    }

    private void clampOffset() {
        if (contentHeight <= windowHeight)
            scrollOffset = 0;
        else
            scrollOffset = Math.max(0, Math.min(contentHeight - windowHeight, scrollOffset));
    }

    private int getThumbY() {
        int thumbHeight = getThumbHeight();
        float scrollProgress = (float) scrollOffset / (contentHeight - windowHeight);
        return (int) (scrollProgress * (windowHeight - thumbHeight));
    }

    private int getThumbHeight() {
        float visibleRatio = (float) windowHeight / contentHeight;
        return Math.max(20, (int) (windowHeight * visibleRatio));
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setContentHeight(int h) {
        this.contentHeight = h;
        clampOffset();
    }

    public void setWindowHeight(int h) {
        this.windowHeight = h;
        clampOffset();
    }
}
