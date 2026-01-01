package me.retucio.camtweaks.ui.widgets;

import me.retucio.camtweaks.ui.screen.ClickGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class Widget {

    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected int x, y, w, h;

    public Widget(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }


    public abstract void render(DrawContext ctx, int mouseX, int mouseY, float delta);


    // input

    public void onKey(int key, int action) {}

    public void mouseClicked(int mouseX, int mouseY, int button) {}

    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
    }

    public void mouseDragged(int mouseX, int mouseY) {}

    public void mouseScrolled(double amount) {}

    public boolean isHovered(int mouseX, int mouseY) {
        if (!ClickGUI.INSTANCE.canSelect(this)) return false;
        return mouseX > x && mouseX < x + w &&
                mouseY > y && mouseY < y + h;
    }


    // getters y setters

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY( int y) {this.y = y; }

    public int getW() { return w; }
    public void setW(int w) { this.w = w; }

    public int getH() { return h; }
    public void setH(int h) { this.h = h; }
}
