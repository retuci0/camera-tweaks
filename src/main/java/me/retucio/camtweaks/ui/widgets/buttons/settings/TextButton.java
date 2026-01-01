package me.retucio.camtweaks.ui.widgets.buttons.settings;

import me.retucio.camtweaks.module.settings.StringSetting;
import me.retucio.camtweaks.ui.widgets.buttons.SettingButton;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;


// botón para los ajustes que requieran introducir texto
@SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
public class TextButton extends SettingButton<StringSetting> {

    private boolean typing;
    private final StringSetting setting;
    private final StringBuilder buffer = new StringBuilder();

    public TextButton(StringSetting setting, SettingsFrame parent, int offset) {
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

        // texto
        String label = setting.getName() + ": " + (typing ? buffer + "_" : setting.getValue());
        ctx.drawText(mc.textRenderer, label, x + 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {
                typing = true;
                buffer.setLength(0);
                buffer.append(setting.getValue());  // precargar valor anterior
            } else if (button == 1 && mc.isShiftPressed()) {
                typing = false;
                setting.reset();
            }
        } else {
            typing = false;
        }
    }

    @Override
    public void onKey(int key, int action) {
        if (!typing || action == GLFW.GLFW_RELEASE) return;

        switch (key) {  // casos para teclas especiales como enter, escape o borrar
            case GLFW.GLFW_KEY_ENTER -> {
                setting.setValue(buffer.toString());
                typing = false;
            } case GLFW.GLFW_KEY_BACKSPACE -> {
                onBackspace();
            } case GLFW.GLFW_KEY_ESCAPE -> {
                typing = false;
            } case GLFW.GLFW_KEY_SPACE -> {
                charTyped(' ');
            } default -> {
                String c = KeyUtil.getKeyName(key);
                if (c.length() == 1) {
                    if (KeyUtil.isShiftDown())
                        charTyped(KeyUtil.shiftKey(c).charAt(0));
                    else
                        charTyped(c.toLowerCase().charAt(0));
                }
            }
        }
    }

    private void charTyped(char c) {
        if (!typing) return;
        buffer.append(c);
        if (mc.textRenderer.getWidth(setting.getName() + ": " + buffer + "_") > w - 8) onBackspace();
    }

    public boolean isFocused() {
        return typing;
    }

    private void onBackspace() {
        if (!buffer.isEmpty())
            buffer.deleteCharAt(buffer.length() - 1);
    }
}