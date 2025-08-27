package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.KeyEvent;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.ui.frames.ClickGUISettingsFrame;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

// slider (barrita) para los ajustes numéricos
public class SliderButton extends SettingButton {

    private final NumberSetting setting;
    private boolean dragging = false;
    public final DecimalFormat df;  // usar formato decimal para el valor del ajuste

    public SliderButton(NumberSetting setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
        this.df = new DecimalFormat("#.##");  // solo mostrar dos décimales
        EVENT_BUS.subscribe(this);  // para poder escuchar teclas
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        ctx.fill(x, y, x + w, y + height, Colors.moduleButtonColor);

        // calcular cómo de llena tendría que estar el "slider"
        double percent = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        int filled = (int) (percent * w);
        ctx.fill(x, y, x + filled, y + height, Colors.sliderFillingColor);

        String label = setting.getName() + ": " + df.format(setting.getValue());
        ctx.drawTextWithShadow(parent.mc.textRenderer, label, x + 5, y + 3, -1);

        // lógica para el arrastre (arrastramiento?) del valor
        if (dragging) {
            double newVal = setting.getMin() + ((mouseX - x) / (double) w) * (setting.getMax() - setting.getMin());
            newVal = Math.max(setting.getMin(), Math.min(setting.getMax(), newVal));
            newVal = Math.round(newVal / setting.getIncrement()) * setting.getIncrement();
            setting.setValue((float) newVal);
        }

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
            if (button == 0) dragging = true;  // arrastrar con el clic izquierdo
            else if (button == 1 && KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) // restablecer al valor por defecto con shift izquerdo + clic derecho
                setting.reset();
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
    }

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        // de no ser de esto, si se cierra la interfaz sin haber soltado el ratón  mientras se arrastraba el valor, al reabrir la interfaz se seguía arrastrando, aún habiendo soltado ya el clic
        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE || event.getKey() == ClickGUISettingsFrame.guiSettings.getKey()) dragging = false;
    }

    public NumberSetting getSetting() {
        return setting;
    }
}