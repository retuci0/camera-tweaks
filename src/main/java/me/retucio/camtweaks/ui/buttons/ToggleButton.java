package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

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
            color = color.brighter();  // simplemente utilizar el método "built-in" de Java para hacer el botón más claro cuando el puntero se encuentra encima de él
            // quizás debería de utilizar .brighter() más, en vez de dar valores personalizados

        ctx.fill(x, y, x + w, y + height, color.getRGB());
        ctx.drawText(parent.mc.textRenderer, setting.getName(), x + 5, y + 3, -1, true);

        // dibujar "tooltips" (cajas de texto) al pasar el puntero encima del botón, para mostrar su descripción
        if (isHovered((int) mouseX, (int) mouseY)) {
            Screen currentScreen = parent.mc.currentScreen;
            if (currentScreen != null)
                ctx.drawTooltip(Text.of(setting.getDescription()), (int) mouseX, (int) mouseY + 20);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY)) {
            if (button == 0) setting.toggle();  // clic izquierdo para alternar
            else if (button == 1 && KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT))   // shift izquierdo + clic derecho para restablecer al valor por defecto
                setting.reset();
        }
    }

    public BooleanSetting getSetting() {
        return setting;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}
}