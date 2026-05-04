package me.retucio.sputnik.ui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class Button extends Widget {

    protected Panel<?> parent;
    protected int offset;

    public Button(Panel<?> parent, int offset) {
        super(parent.getX(), parent.getRenderY(), parent.getW(), parent.getH());
        this.parent = parent;
        this.offset = offset;
    }


    public abstract void drawTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY);

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Panel<?> getParent() {
        return parent;
    }
}
