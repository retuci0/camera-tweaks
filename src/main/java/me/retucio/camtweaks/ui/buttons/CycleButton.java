package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// botón para los ajustes tipo EnumSetting (de modo)
public class CycleButton<E extends Enum<E>> extends SettingButton {

    private final EnumSetting<E> setting;

    public CycleButton(EnumSetting<E> setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        int bgColor = isHovered((int) mouseX, (int) mouseY)
                ? Colors.hoveredSettingButtonColor
                : Colors.settingButtonColor;

        ctx.fill(x, y, x + w, y + height, bgColor); // fondo del botón

        // texto del botón: nombre + valor de texto del enum
        String label = setting.getName() + ": " + setting.getValue().toString();
        ctx.drawTextWithShadow(parent.mc.textRenderer, label, x + 5, y + 3, -1);

        // tooltip
        if (isHovered((int) mouseX, (int) mouseY)) {
            Screen currentScreen = parent.mc.currentScreen;
            if (currentScreen != null) {
                if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    List<Text> lines = new ArrayList<>();
                    lines.add(Text.literal("modos disponibles:"));
                    for (Enum<?> val : setting.getValues()) {
                        if (val == setting.getValue())
                            lines.add(Text.literal("> " + val.toString() + " <").formatted(Formatting.GREEN));
                        else
                            lines.add(Text.literal(val.toString()));
                    }
                    ctx.drawTooltip(parent.mc.textRenderer, lines, (int) mouseX, (int) mouseY + 20);
                } else {
                    ctx.drawTooltip(Text.of(setting.getDescription()), (int) mouseX, (int) mouseY + 20);
                }
            }
        }
    }


    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY)) {
            if (button == 0) {
                setting.cycle();  // clic izquierdo -> ciclar
            } else if (button == 1) {
                // shift + clic derecho -> restablecer
                if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT))
                    setting.reset();
                else
                    setting.cycleBackwards();  // simplemente clic derecho -> ciclar valores pero hacia atrás
            }
        }
    }

    public EnumSetting<E> getSetting() {
        return setting;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}
}
