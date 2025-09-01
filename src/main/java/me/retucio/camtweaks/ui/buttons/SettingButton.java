package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.settings.AbstractSetting;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import net.minecraft.client.gui.DrawContext;

// clase base para los botones de los ajustes
public abstract class SettingButton {

    protected final AbstractSetting setting;
    protected final SettingsFrame parent;
    protected int offset;
    protected final int height = 15;

    protected int x, y, w, h;

    public SettingButton(AbstractSetting setting, SettingsFrame parent, int offset) {
        this.setting = setting;
        this.parent = parent;
        this.offset = offset;
    }

    public abstract void render(DrawContext ctx, double mouseX, double mouseY, float delta);
    public abstract void mouseClicked(double mouseX, double mouseY, int button);
    public abstract void mouseReleased(double mouseX, double mouseY, int button);

    protected boolean isHovered(double mouseX, double mouseY) {
        return isHovered((int) mouseX, (int) mouseY);
    }

    protected boolean isHovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + w &&
               mouseY > y && mouseY < y + h;
    }

    public AbstractSetting getSetting() {
        return setting;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }
}