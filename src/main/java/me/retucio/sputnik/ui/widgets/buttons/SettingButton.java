package me.retucio.sputnik.ui.widgets.buttons;

import me.retucio.sputnik.module.setting.Setting;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import me.retucio.sputnik.ui.widgets.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

// clase base para los botones de los ajustes
public abstract class SettingButton<S extends Setting<?>> extends Button {

    protected S setting;

    public SettingButton(S setting, SettingsFrame parent, int offset) {
        super(parent, offset);
        this.setting = setting;
    }

    // dibujar "tooltips" (cajitas de texto bajo el puntero del ratón)
    // con la descripción para asistir al usuario en el caso de que tenga down
    @Override
    public void drawTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY))
            gui.setTooltipForNextFrame(Component.literal(setting.getDescription()), mouseX, mouseY + 20);
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return setting.getSg().isExtended()
                && super.isHovered(mouseX, mouseY);
    }

    public S getSetting() {
        return setting;
    }
}