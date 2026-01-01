package me.retucio.camtweaks.ui.widgets.buttons.settings;

import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.widgets.buttons.SettingButton;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;
import me.retucio.camtweaks.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.text.DecimalFormat;


// slider (barrita) para los ajustes numéricos
public class SliderButton extends SettingButton<NumberSetting> {

    private boolean dragging = false;
    public final DecimalFormat df;  // usar formato decimal para el valor del ajuste

    public SliderButton(NumberSetting setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
        this.df = new DecimalFormat("#.##");  // solo mostrar dos décimales
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // fondo
        Color bgColor = Colors.buttonColor;
        Color fillingColor = Colors.mainColor;

        if (isHovered(mouseX, mouseY)) {
            bgColor = bgColor.brighter();
            fillingColor = fillingColor.brighter();
        }

        ctx.fill(x, y, x + w, y + h, bgColor.getRGB());

        // calcular cómo de lleno tendría que estar el "slider"
        double percent = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        int filled = (int) (percent * w);
        ctx.fill(x, y, x + filled, y + h, fillingColor.getRGB());

        String label = setting.getName() + ": " + df.format(setting.getValue());
        ctx.drawText(mc.textRenderer, label, x + 5, y + 3, -1, true);

        // lógica para arrastrar el valor
        if (dragging) {
            double newVal = setting.getMin() + ((mouseX - x) / (double) w) * (setting.getMax() - setting.getMin());
            newVal = Math.max(setting.getMin(), Math.min(setting.getMax(), newVal));
            newVal = Math.round(newVal / setting.getIncrement()) * setting.getIncrement();
            setting.setValue((float) newVal);
        }
    }

    @Override
    public void onKey(int key, int action) {
        // de no ser de esto, si se cierra la interfaz sin haber soltado el ratón mientras se arrastraba el valor,
        // al reabrir la interfaz se seguía arrastrando, aun habiendo soltado ya el clic
        if (key == GLFW.GLFW_KEY_ESCAPE
                || key == ClientSettingsFrame.guiSettings.getKey())
            dragging = false;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) dragging = true;  // arrastrar con el clic izquierdo
            else if (button == 1 && KeyUtil.isShiftDown())
                // restablecer al valor por defecto con shift + clic derecho
                setting.reset();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if (button == 0) dragging = false;
    }
}