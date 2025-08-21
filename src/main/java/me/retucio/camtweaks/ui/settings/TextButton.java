package me.retucio.camtweaks.ui.settings;

import me.retucio.camtweaks.module.settings.StringSetting;
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
        int x = parent.x + 4;
        int y = parent.y + offset + 3;
        int w = parent.w - 8;
        int bgColor = isHovered((int) mouseX, (int) mouseY)
                ? Colors.hoveredCycleButtonColor
                : Colors.cycleButtonColor;

        ctx.fill(x, y, x + w, y + height, bgColor);

        String label = setting.getName() + ": " + (typing ? buffer.toString() + "_" : setting.getValue());
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
        if (!typing || action != GLFW.GLFW_PRESS) return;

        switch (key) {  // casos para teclas especiales como enter, escape o borrar
            case GLFW.GLFW_KEY_ENTER -> {
                setting.setValue(buffer.toString());
                typing = false;
            } case GLFW.GLFW_KEY_BACKSPACE -> {
                buffer.deleteCharAt(buffer.length() - 1);
            } case GLFW.GLFW_KEY_ESCAPE -> {
                typing = false;
            } default -> {
                String c = KeyUtil.getKeyName(key);
                if (c.length() == 1) {
                    if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || KeyUtil.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT))
                        charTyped(c.toUpperCase().charAt(0));
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

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

}
