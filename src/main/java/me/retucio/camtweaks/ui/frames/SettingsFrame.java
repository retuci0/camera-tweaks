package me.retucio.camtweaks.ui.frames;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.SettingsFrameEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.*;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.buttons.*;
import me.retucio.camtweaks.ui.buttons.SliderButton;
import me.retucio.camtweaks.ui.buttons.TextButton;
import me.retucio.camtweaks.ui.buttons.ToggleButton;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// marco para los botones de los ajustes de cada módulo
public class SettingsFrame {

    final String title;
    public Module module;
    final List<SettingButton> settingButtons = new ArrayList<>();

    public List<SettingButton> visibleButtons = new ArrayList<>();

    public int x, y, w, h;
    public int renderY;
    int dragX, dragY;
    boolean dragging;
    public int totalHeight = 0;

    public final MinecraftClient mc = MinecraftClient.getInstance();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SettingsFrame(Module module, int x, int y, int w, int h) {
        this.module = module;

        // asegurarse de que no se sale de la pantalla
        if (mc.getWindow() != null && x + w > mc.getWindow().getScaledWidth())
            this.x = mc.getWindow().getScaledWidth() - w;
        else
            this.x = x;

        this.y = y;
        this.w = w;
        this.h = h;

        // añadir el respectivo tipo de botón de cada ajuste a la lista de botones
        int offset = h;
        for (Setting setting : module.getSettings()) {
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
                } case ListSetting l -> {
                    addButton(new ListButton<>(l, this, offset));
                    offset += 18;
                } case ColorSetting c -> {
                    addButton(new ColorButton(c, this, offset));
                    offset += 18;
                } case OptionSetting o -> {
                    addButton(new ChooseButton<>(o, this, offset));
                    offset += 18;
                }
                default -> {}
            }
        }
        title = "ajustes de " + module.getName();

        visibleButtons = settingButtons.stream()
                .filter(sb -> sb.getSetting().isVisible())
                .toList();
    }

    public void addButton(SettingButton button) {
        settingButtons.add(button);
        updateWidth();  // actualizar anchura del frame tras cada iteración
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateWidth();

        // cabezal
        int centerX = x + w / 2;
        int centerY = renderY + h / 2;
        int titleX = x + 8;
        int titleY = centerY - mc.textRenderer.fontHeight / 2;

        int headerColor = module.isEnabled()
                ? Colors.enabledToggleButtonColor.getRGB()
                : Colors.disabledToggleButtonColor.getRGB();

        int closeButtonColor = isCloseButtonHovered(mouseX, mouseY)
                ? Color.RED.getRGB()
                : -1;

        if (!module.shouldSaveSettings()) headerColor = (int) module.getSettings().stream().filter(s -> ((BooleanSetting) s).isEnabled()).count() > 0
                ? Colors.enabledToggleButtonColor.getRGB()
                : Colors.disabledToggleButtonColor.getRGB();

        ctx.fill(x, renderY, x + w, renderY + h, headerColor);
        ctx.drawText(mc.textRenderer, title, titleX, titleY, -1, true);
        ctx.drawText(mc.textRenderer, "×", x + w - mc.textRenderer.getWidth("×") - 8, titleY, closeButtonColor, true);

        visibleButtons = settingButtons.stream()
                .filter(sb -> sb.getSetting().isVisible() && sb.getSetting().isSearchMatch())
                .toList();

        // descripción
        List<String> descLines = wrapDescription(module.getDescription(), w - 8);
        int lineSpacing = 2;

        int descHeight = descLines.size() * mc.textRenderer.fontHeight + (descLines.size() - 1) * lineSpacing;
        int descBoxTop = renderY + h + 5;
        int descBoxBottom = descBoxTop + descHeight + 8;

        totalHeight = descHeight + 8 + h * visibleButtons.size() + 4;

        ctx.fill(x, descBoxTop - 4, x + w, descBoxTop + totalHeight, Colors.frameBGColor.getRGB());
        ctx.fill(x + 4, descBoxTop, x + w - 4, descBoxBottom, Colors.buttonColor.getRGB());

        int firstLineY = descBoxTop + ((descBoxBottom - descBoxTop) - descHeight) / 2;
        for (int i = 0; i < descLines.size(); i++) {
            String line = descLines.get(i);
            int lineX = centerX - mc.textRenderer.getWidth(line) / 2;
            int lineY = firstLineY + i * (mc.textRenderer.fontHeight + lineSpacing);
            ctx.drawText(mc.textRenderer, line, lineX, lineY, -1, true);
        }

        int startButtonY = firstLineY - descBoxTop + descBoxBottom;
        for (SettingButton sb : visibleButtons) {
            sb.setX(x + 4);
            sb.setY(startButtonY);
            sb.setW(w - 8);
            sb.setH(h - h / 4);
            sb.render(ctx, mouseX, mouseY, delta);
            startButtonY += h;
        }
    }

    public void drawTooltips(DrawContext ctx, double mouseX, double mouseY) {
        for (SettingButton sb : visibleButtons)
            sb.drawTooltip(ctx, mouseX, mouseY);
    }


    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (isCloseButtonHovered(mouseX, mouseY)) {
                ClickGUI.INSTANCE.closeSettingsFrame(this.module);
                return;
            } if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {
                ClickGUI.INSTANCE.closeSettingsFrame(module);
            }
        }

        for (SettingButton sb : visibleButtons)
            sb.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
        if (button == 0) {
            dragging = false;
            CameraTweaks.EVENT_BUS.post(new SettingsFrameEvent.Move(this));
        }

        for (SettingButton sb : visibleButtons)
            sb.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseDragged(double mouseX, double mouseY) {
        for (SettingButton sb : visibleButtons)
            sb.mouseDragged(mouseX, mouseY);
    }

    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w &&
                mouseY > renderY && mouseY < renderY + h;
    }

    public boolean isCloseButtonHovered(double mouseX, double mouseY) {
        return mouseX > x + w - mc.textRenderer.getWidth("×") - 12 && mouseX < x + w - 4
                && mouseY > renderY + 4 && mouseY < renderY + h - 4;
    }

    @SuppressWarnings("rawtypes")
    void updateWidth() {
        // asegurarse de que todos los botones caben en el marco, haciendo que la anchura se ajuste al texto más largo
        if (mc.textRenderer == null) return;

        int maxWidth = mc.textRenderer.getWidth(title);
        for (SettingButton button : settingButtons) {
            String text = button.getSetting().getName();

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
        this.w = maxWidth + 30;
    }

    private List<String> wrapDescription(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (mc.textRenderer == null) return lines;

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.isEmpty() ? word : line + " " + word;
            if (mc.textRenderer.getWidth(testLine) > maxWidth - 8) { // padding of 4 each side
                if (!line.isEmpty()) lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    public List<SettingButton> getButtons() {
        return settingButtons;
    }

    public void updateRenderY(int scrollOffset) {
        renderY = y - scrollOffset;
    }
}
