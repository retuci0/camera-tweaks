package me.retucio.camtweaks.ui.widgets.buttons.settings;

import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.ui.widgets.buttons.SettingButton;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;


// botón para los ajustes booleanos (funciona como un interruptor)
public class ToggleButton extends SettingButton<BooleanSetting> {

    private final BooleanSetting setting;

    public ToggleButton(BooleanSetting setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // fondo
        Color color = setting.isEnabled()
                ? Colors.enabledToggleButtonColor
                : Colors.disabledToggleButtonColor;

        if (isHovered(mouseX, mouseY))
            color = color.brighter();

        ctx.fill(x, y, x + w, y + h, color.getRGB());

        // texto
        ctx.drawText(mc.textRenderer, setting.getName(), x + 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) setting.toggle();  // clic izquierdo para alternar
            else if (button == 1 && KeyUtil.isShiftDown())
                // shift + clic derecho para restablecer al valor por defecto
                setting.reset();
        }
    }
}