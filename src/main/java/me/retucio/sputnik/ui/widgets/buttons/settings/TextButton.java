package me.retucio.sputnik.ui.widgets.buttons.settings;

import me.retucio.sputnik.module.setting.settings.StringSetting;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import me.retucio.sputnik.util.MiscUtil;
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

        // pegar con ctrl + v
        if (key == GLFW.GLFW_KEY_V && KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            paste();
            return;
        }

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

    public void onBackspace() {
        MiscUtil.backspace(buffer);
    }

    public void paste() {
        buffer.append(MiscUtil.getPasteContent(setting.getMaxLength() - buffer.length()));
    }
}