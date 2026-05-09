package me.retucio.sputnik.ui.widgets.panels;

import me.retucio.sputnik.ui.widgets.Button;
import me.retucio.sputnik.ui.widgets.Panel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public abstract class ExtendablePanel<B extends Button> extends Panel<B> {
    protected boolean extended;

    public ExtendablePanel(String title, int x, int y, int w, int h) {
        super(title, x, y, w, h);
        this.extended = true;
    }

    protected void drawExpandCollapse(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        String symbol = extended ? "-" : "+";
        int textWidth = mc.font.width(symbol);
        gui.text(mc.font, Component.literal(symbol),
                x + w - textWidth - 8,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);
    }

    protected boolean isExpandButtonHovered(int mouseX, int mouseY) {
        String symbol = extended ? "-" : "+";
        int textWidth = mc.font.width(symbol);
        int buttonX = x + w - textWidth - 8;
        int buttonY = renderY + (h / 2) - (mc.font.lineHeight / 2);
        return mouseX >= buttonX && mouseX <= buttonX + textWidth &&
                mouseY >= buttonY && mouseY <= buttonY + mc.font.lineHeight;
    }

    public void toggleExtended() {
        extended = !extended;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }
}