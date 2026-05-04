package me.retucio.sputnik.ui.widgets.buttons.settings;

import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;


// botón para los ajustes booleanos (funciona como un interruptor)
public class ToggleButton extends SettingButton<BooleanSetting> {

    private final BooleanSetting setting;

    public ToggleButton(BooleanSetting setting, SettingsPanel parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        // fondo
        Color color = setting.getValue()
                ? Colors.enabledToggleButtonColor
                : Colors.disabledToggleButtonColor;

        if (isHovered(mouseX, mouseY))
            color = color.brighter();

        gui.fill(x, y, x + w, y + h, color.getRGB());

        // texto
        gui.text(mc.font, setting.getName(), x + 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            if (button == 0) setting.toggle();  // clic izquierdo para alternar
            else if (button == 1 && KeyUtil.isShiftDown())
                // shift + clic derecho para restablecer al valor por defecto
                setting.reset();
        }
    }
}