package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.settings.Setting;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

// clase base para los botones de los ajustes
public abstract class SettingButton {

    protected final Setting setting;
    protected final SettingsFrame parent;
    protected int offset;

    protected int x, y, w, h;

    public SettingButton(Setting setting, SettingsFrame parent, int offset) {
        this.setting = setting;
        this.parent = parent;
        this.offset = offset;
    }

    public abstract void render(DrawContext ctx, double mouseX, double mouseY, float delta);
    public abstract void mouseClicked(double mouseX, double mouseY, int button);
    public abstract void mouseReleased(double mouseX, double mouseY, int button);
    public abstract void mouseDragged(double mouseX, double mouseY);

    // dibujar "tooltips" (cajitas de texto bajo el puntero del ratón) con la descripción para asistir al usuario en el caso de que tenga down
    public void drawTooltip(DrawContext ctx, double mouseX, double mouseY) {
        if (isHovered((int) mouseX, (int) mouseY))
            ctx.drawTooltip(Text.of(setting.getDescription()), (int) mouseX, (int) mouseY + 20);
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return isHovered((int) mouseX, (int) mouseY);
    }

    protected boolean isHovered(int mouseX, int mouseY) {
        if (!ClickGUI.INSTANCE.canSelect(this)) return false;
        return mouseX > x && mouseX < x + w &&
               mouseY > y && mouseY < y + h;
    }

    public Setting getSetting() {
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