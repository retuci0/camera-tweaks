package me.retucio.sputnik.ui.widgets.panels;

import me.retucio.sputnik.ui.widgets.Button;
import me.retucio.sputnik.ui.widgets.Panel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public abstract class CloseablePanel<B extends Button> extends Panel<B> {

    protected static final int PADDING = 4;

    public CloseablePanel(String title, int x, int y, int w, int h) {
        super(title, x, y, w, h);
    }

    // dibujar la "x"
    protected void drawCloseButton(GuiGraphicsExtractor gui, int mouseX, int mouseY, int color) {
        gui.text(mc.font, Component.literal("×"),
                x + w - mc.font.width("×") - 2 * PADDING,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                color, true);
    }

    protected boolean isCloseButtonHovered(int mouseX, int mouseY) {
        int closeX = x + w - mc.font.width("×") - 2 * PADDING;
        int closeY = renderY + (h / 2) - (mc.font.lineHeight / 2);
        return mouseX >= closeX && mouseX <= closeX + mc.font.width("×") &&
                mouseY >= closeY && mouseY <= closeY + mc.font.lineHeight;
    }

    public abstract void onClose();
}