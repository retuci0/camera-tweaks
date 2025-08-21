package me.retucio.camtweaks.ui.settings;

import me.retucio.camtweaks.module.settings.Setting;
import net.minecraft.client.gui.DrawContext;

// clase base para los botones de los ajustes
public abstract class SettingButton {

    protected final Setting setting;
    protected final SettingsFrame parent;
    protected int offset;
    protected final int height = 15;

    public SettingButton(Setting setting, SettingsFrame parent, int offset) {
        this.setting = setting;
        this.parent = parent;
        this.offset = offset;
    }

    public abstract void render(DrawContext ctx, double mouseX, double mouseY, float delta);
    public abstract void mouseClicked(double mouseX, double mouseY, int button);
    public abstract void mouseReleased(double mouseX, double mouseY, int button);

    protected boolean isHovered(int mouseX, int mouseY) {
        return mouseX > parent.x + 4 && mouseX < parent.x + parent.w - 4 &&
                mouseY > parent.y + offset && mouseY < parent.y + offset + height;
    }
}