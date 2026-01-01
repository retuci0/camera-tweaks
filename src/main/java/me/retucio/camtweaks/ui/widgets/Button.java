package me.retucio.camtweaks.ui.widgets;

import me.retucio.camtweaks.ui.screen.ClickGUI;
import net.minecraft.client.gui.DrawContext;

public abstract class Button extends Widget {

    protected Frame<?> parent;
    protected int offset;

    public Button(Frame<?> parent, int offset) {
        super(parent.getX(), parent.getRenderY(), parent.getW(), parent.getH());
        this.parent = parent;
        this.offset = offset;
    }


    public abstract void drawTooltip(DrawContext ctx, int mouseX, int mouseY);

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
