package me.retucio.camtweaks.ui.widgets;

import me.retucio.camtweaks.module.settings.Setting;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public abstract class Button {

    protected Frame parent;
    protected int offset;

    protected int x, y, w, h;

    public Button(Frame parent, int offset) {
        this.parent = parent;
        this.offset = offset;
    }

    public abstract void render(DrawContext ctx, int mouseX, int mouseY, float delta);

    public abstract void mouseClicked(int mouseX, int mouseY, int button);
    public abstract void mouseDragged(int mouseX, int mouseY);
    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
    }

    public abstract void drawTooltip(DrawContext ctx, double mouseX, double mouseY);

    protected boolean isHovered(int mouseX, int mouseY) {
        if (!ClickGUI.INSTANCE.canSelect(this)) return false;
        return mouseX > x && mouseX < x + w &&
                mouseY > y && mouseY < y + h;
    }
}
