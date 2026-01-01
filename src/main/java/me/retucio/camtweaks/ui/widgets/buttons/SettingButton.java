package me.retucio.camtweaks.ui.widgets.buttons;

import me.retucio.camtweaks.module.settings.Setting;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;
import me.retucio.camtweaks.ui.widgets.Button;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

// clase base para los botones de los ajustes
public abstract class SettingButton<S extends Setting> extends Button {

    protected S setting;

    public SettingButton(S setting, SettingsFrame parent, int offset) {
        super(parent, offset);
        this.setting = setting;
    }

    // dibujar "tooltips" (cajitas de texto bajo el puntero del ratón)
    // con la descripción para asistir al usuario en el caso de que tenga down
    @Override
    public void drawTooltip(DrawContext ctx, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY))
            ctx.drawTooltip(Text.of(setting.getDescription()), mouseX, mouseY + 20);
    }

    public S getSetting() {
        return setting;
    }
}