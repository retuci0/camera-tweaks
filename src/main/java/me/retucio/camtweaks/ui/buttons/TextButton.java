package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.settings.StringSetting;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

// botón para los ajustes que requieran introducir texto
public class TextButton extends SettingButton {

    private boolean typing;
    private final StringSetting setting;
    private final StringBuilder buffer = new StringBuilder();

    public TextButton(StringSetting setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        int bgColor = isHovered((int) mouseX, (int) mouseY)
                ? Colors.hoveredSettingButtonColor
                : Colors.settingButtonColor;

        ctx.fill(x, y, x + w, y + height, bgColor);

        String label = setting.getName() + ": " + (typing ? buffer + "_" : setting.getValue());
        ctx.drawTextWithShadow(parent.mc.textRenderer, label, x + 5, y + 3, -1);

        if (isHovered((int) mouseX, (int) mouseY)) {
            if (parent.mc.currentScreen != null)
                ctx.drawTooltip(Text.of(setting.getDescription()), (int) mouseX, (int) mouseY + 20);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY)) {
            if (button == 0) {
                typing = true;
                buffer.setLength(0);
                buffer.append(setting.getValue());  // precargar valor anterior
            } else if (button == 1 && KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                typing = false;
                setting.reset();
            }
        } else {
            typing = false;
        }
    }

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
            } default -> {
                String c = KeyUtil.getKeyName(key);
                if (c.length() == 1) {
                    if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || KeyUtil.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT))
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
    }

    public boolean isFocused() {
        return typing;
    }

    public StringSetting getSetting() {
        return setting;
    }

    private void onBackspace() {
        if (!buffer.isEmpty()) buffer.deleteCharAt(buffer.length() - 1);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}
}