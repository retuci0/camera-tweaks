package me.retucio.camtweaks.ui.widgets.buttons.settings;

import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.widgets.buttons.SettingButton;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;


// botón para los ajustes tipo EnumSetting (de modo)
public class CycleButton<E extends Enum<E>> extends SettingButton<EnumSetting<E>> {

    public CycleButton(EnumSetting<E> setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // fondo
        int bgColor = isHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();

        ctx.fill(x, y, x + w, y + h, bgColor);

        // texto del botón: nombre + valor de texto del enum
        String label = setting.getName() + ": " + setting.getValue().toString();
        ctx.drawText(mc.textRenderer, label, x + 5, y + 3, -1, true);
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
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
    public void drawTooltip(DrawContext ctx, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            Screen currentScreen = mc.currentScreen;

            if (currentScreen != null) {
                if (mc.isShiftPressed()) {
                    // lista de opciones disponibles
                    List<Text> lines = new ArrayList<>();
                    lines.add(Text.literal("modos disponibles:"));

                    for (Enum<?> val : setting.getValues()) {
                        if (val == setting.getValue())
                            lines.add(Text.literal("> " + val.toString() + " <").formatted(Formatting.GREEN));
                        else
                            lines.add(Text.literal(val.toString()));
                    }

                    ctx.drawTooltip(mc.textRenderer, lines, mouseX, mouseY + 20);
                } else {
                    // descripción normal del ajuste
                    ctx.drawTooltip(Text.of(setting.getDescription()), mouseX, mouseY + 20);
                }
            }
        }
    }
}
