package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

import static me.retucio.camtweaks.CameraTweaks.mc;

// botón para los ajustes booleanos (funciona como un interruptor)
public class ToggleButton extends SettingButton {

    private final BooleanSetting setting;

    public ToggleButton(BooleanSetting setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        Color color = setting.isEnabled()
                ? Colors.enabledToggleButtonColor
                : Colors.disabledToggleButtonColor;

        if (isHovered((int) mouseX, (int) mouseY))
            color = color.brighter();

        ctx.fill(x, y, x + w, y + h, color.getRGB());
        ctx.drawText(parent.mc.textRenderer, setting.getName(), x + 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) setting.toggle();  // clic izquierdo para alternar
            else if (button == 1 && mc.isShiftPressed())   // shift izquierdo + clic derecho para restablecer al valor por defecto
                setting.reset();
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY) {}

    public BooleanSetting getSetting() {
        return setting;
    }
}