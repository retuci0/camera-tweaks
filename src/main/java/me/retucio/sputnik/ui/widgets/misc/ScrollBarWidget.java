package me.retucio.sputnik.ui.widgets.misc;

import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel;
import me.retucio.sputnik.ui.widgets.Widget;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;


public class ScrollBarWidget extends Widget {

    private boolean dragging;
    private int dragY, scrollStart;
    private int scrollOffset = 0;

    private int contentHeight, windowHeight;

    public ScrollBarWidget() {
        // las coordenadas no importan porque está estático
        super(0, 0, 0, 0);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        if (contentHeight <= windowHeight || !ClientSettingsPanel.clientSettings.scrollBar.getValue()) return;

        int trackX1 = mc.getWindow().getGuiScaledWidth() - 10;
        int trackX2 = trackX1 + 10;

        gui.fill(trackX1, 0, trackX2, windowHeight, Colors.buttonColor.getRGB());

        int thumbHeight = getThumbHeight();
        int thumbY = getThumbY();

        Color thumbColor = isThumbHovered(mouseX, mouseY)
                ? Colors.mainColor.brighter()
                : Colors.mainColor;

        if (dragging) thumbColor = Colors.mainColor.darker();

        gui.fill(trackX1 + 1, thumbY + 1, trackX2 - 1, thumbY + thumbHeight - 1, thumbColor.getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || !ClientSettingsPanel.clientSettings.scrollBar.getValue()) return;
        if (isThumbHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            dragging = true;
            dragY = mouseY;
            scrollStart = scrollOffset;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGui.INSTANCE.unselect(this);
        dragging = false;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY) {
        if (!dragging || !ClientSettingsPanel.clientSettings.scrollBar.getValue()) return;

        int thumbHeight = getThumbHeight();
        int trackHeight = windowHeight - thumbHeight;

        float ratio = (float) (contentHeight - windowHeight) / trackHeight;
        int deltaY = mouseY - dragY;
        scrollOffset = scrollStart + (int) (deltaY * ratio);
        clampOffset();
    }

    @Override
    public void mouseScrolled(double amount) {
        if (contentHeight <= windowHeight) return;
        scrollOffset -= (int) amount * 20;
        clampOffset();
    }

    private boolean isThumbHovered(double mouseX, double mouseY) {
        if (!ClickGui.INSTANCE.canSelect(this)) return false;
        int trackX1 = mc.getWindow().getGuiScaledWidth() - 10;
        int trackX2 = trackX1 + 10;
        int thumbY = getThumbY();
        int thumbHeight = getThumbHeight();
        return mouseX >= trackX1 && mouseX <= trackX2 && mouseY >= thumbY && mouseY <= thumbY + thumbHeight;
    }

    private void clampOffset() {
        if (contentHeight <= windowHeight)
            scrollOffset = 0;
        else
            scrollOffset = Math.clamp(scrollOffset, 0, contentHeight - windowHeight);
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
