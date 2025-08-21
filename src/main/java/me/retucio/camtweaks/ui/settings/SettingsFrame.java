package me.retucio.camtweaks.ui.settings;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.*;
import me.retucio.camtweaks.ui.ClickGUI;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// marco para los botones de los ajustes de cada módulo
public class SettingsFrame {

    private final String title;
    public final Module module;
    final List<SettingButton> settingButtons = new ArrayList<>();

    public int x, y, w, h;
    boolean dragging;
    int dragX;
    int dragY;

    final MinecraftClient mc = MinecraftClient.getInstance();

    public SettingsFrame(Module module, int x, int y, int w, int h) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        // añadir el respectivo tipo de botón de cada ajuste a la lista de botones
        int offset = h;
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            switch (setting) {
                case BooleanSetting b -> {
                    addButton(new ToggleButton(b, this, offset));
                    offset += 18;
                } case NumberSetting n -> {
                    addButton(new SliderButton(n, this, offset));
                    offset += 18;
                } case EnumSetting e -> {
                    addButton(new CycleButton<>(e, this, offset));
                    offset += 18;
                } case KeySetting k -> {
                    addButton(new BindButton(k, this, offset));
                    offset += 18;
                } case StringSetting s -> {
                    addButton(new TextButton(s, this, offset));
                    offset += 18;
                } default -> {}
            }
        }
        title = "ajustes de " + module.getName() + " -";
    }

    public void addButton(SettingButton button) {
        settingButtons.add(button);
        updateWidth();  // actualizar anchura del frame tras cada iteración
    }

    void updateWidth() {
        // asegurarse de que todos los botones caben en el marco, haciendo que la anchura se ajuste al texto más largo
        int maxWidth = mc.textRenderer.getWidth(title);
        for (SettingButton button : settingButtons) {
            String text = button.setting.getName();

            switch (button) {
                case SliderButton sliderButton -> text += ": " + sliderButton.df.format((sliderButton.getSetting()).getValue());
                case CycleButton cycleButton -> text += ": " + cycleButton.getSetting().getValue();
                case BindButton bindButton -> text += ": " + bindButton.getSetting().getKeyName();
                case TextButton textButton -> text += ": " + textButton.getSetting().getValue();
                default -> {}
            }

            int textWidth = mc.textRenderer.getWidth(text);
            maxWidth = Math.max(maxWidth, textWidth);
        }
        this.w = maxWidth + 22;
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateWidth();
        ctx.fill(x, y, x + w, y + h, module.isEnabled()
                ? Colors.enabledToggleButtonColor.getRGB()
                : Colors.disabledToggleButtonColor.getRGB());

        ctx.drawText(mc.textRenderer, title,
                x + (w / 2) - (mc.textRenderer.getWidth(title) / 2),
                y + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, false);

        // si no hay ajustes disponibles para un módulo, hacerle saber al usuario
        if (settingButtons.isEmpty()) {
            String msg = "no hay ajustes :(";
            ctx.fill(x, y + h, x + w, y + 2 * h, Colors.frameBGColor);

            ctx.drawText(mc.textRenderer, msg,
                    x + (w / 2) - (mc.textRenderer.getWidth(msg) / 2),
                    (int) (y + h * 1.25),
                    -1, false);
            return;
        }

        int totalHeight = settingButtons.size() * h + 3;
        ctx.fill(x, y + h + 1, x + w, y + h + totalHeight, Colors.frameBGColor);

        for (SettingButton sb : settingButtons)
            if (sb.setting.isVisible()) sb.render(ctx, mouseX, mouseY, delta);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {
                ClickGUI.INSTANCE.closeSettingsFrame(module);
            }
        }

        for (SettingButton sb : settingButtons)
            if (sb.setting.isVisible()) sb.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseRelease(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        for (SettingButton sb : settingButtons)
            if (sb.setting.isVisible()) sb.mouseReleased(mouseX, mouseY, button);
    }

    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }

    boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w &&
                mouseY > y && mouseY < y + h;
    }

    public List<SettingButton> getButtons() {
        return settingButtons;
    }
}
