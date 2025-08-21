package me.retucio.camtweaks.ui.settings;

import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

// botón para configurar la tecla asignada a un módulo. comienza a escuchar al hacerle clic
public class BindButton extends SettingButton {

    private final KeySetting setting;
    private boolean listening = false;

    public BindButton(KeySetting setting, SettingsFrame parent, int offset) {
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
        String label = setting.getName() + ": " + (listening ? "..." : setting.getKeyName());
        ctx.drawTextWithShadow(parent.mc.textRenderer, label, x + 5, y + 3, -1);

        if (isHovered((int) mouseX, (int) mouseY)) {
            Screen currentScreen = parent.mc.currentScreen;
            if (currentScreen != null)
                ctx.drawTooltip(Text.of(setting.getDescription()), (int) mouseX, (int) mouseY + 20);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY)) {
            if (button == 0) listening = !listening;
            else if (button == 1 && KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT))
                setting.reset();
        } else {
            listening = false;
        }
    }

    public void onKey(int key, int action) {
        if (listening && action == GLFW.GLFW_PRESS) {
            if (key == GLFW.GLFW_KEY_ESCAPE)
                setting.setKey(GLFW.GLFW_KEY_UNKNOWN);
            else
                setting.setKey(key);
            listening = false;
        }
    }

    public boolean isFocused() {
        return listening;
    }

    public KeySetting getSetting() {
        return setting;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}
}