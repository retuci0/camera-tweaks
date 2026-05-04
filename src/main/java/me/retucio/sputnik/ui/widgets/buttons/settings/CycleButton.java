package me.retucio.sputnik.ui.widgets.buttons.settings;

import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;


// botón para los ajustes tipo EnumSetting (de modo)
public class CycleButton<E extends Enum<E>> extends SettingButton<EnumSetting<E>> {

    public CycleButton(EnumSetting<E> setting, SettingsPanel parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        // fondo
        int bgColor = isHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();

        gui.fill(x, y, x + w, y + h, bgColor);

        // texto del botón: nombre + valor de texto del enum
        String label = setting.getName() + ": " + setting.getValue().toString();
        gui.text(mc.font, label, x + 5, y + 3, -1, true);
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            if (button == 0) {
                setting.cycle();  // clic izquierdo -> ciclar
            } else if (button == 1) {
                // shift + clic derecho -> restablecer
                if (KeyUtil.isShiftDown())
                    setting.reset();
                else  // solamente clic derecho -> ciclar valores pero hacia atrás
                    setting.cycleBackwards();
            }
        }
    }

    @Override
    public void drawTooltip(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            Screen currentScreen = mc.screen;

            if (currentScreen != null) {
                if (KeyUtil.isShiftDown()) {
                    // lista de opciones disponibles
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.literal("modos disponibles:"));

                    for (Enum<?> val : setting.getValues()) {
                        if (val == setting.getValue())
                            lines.add(Component.literal("> " + val.toString() + " <").withStyle(ChatFormatting.GREEN));
                        else
                            lines.add(Component.literal(val.toString()));
                    }

                    ctx.setComponentTooltipForNextFrame(mc.font, lines, mouseX, mouseY + 20);
                } else {
                    // descripción normal del ajuste
                    ctx.setTooltipForNextFrame(Component.literal(setting.getDescription()), mouseX, mouseY + 20);
                }
            }
        }
    }
}
