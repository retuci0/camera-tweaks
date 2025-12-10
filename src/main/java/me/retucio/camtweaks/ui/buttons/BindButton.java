package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import static me.retucio.camtweaks.CameraTweaks.mc;

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
        int bgColor = isHovered((int) mouseX, (int) mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();

        ctx.fill(x, y, x + w, y + h, bgColor);
        String label = setting.getName() + ": " + (listening ? "..." : setting.getKeyName());
        ctx.drawText(parent.mc.textRenderer, label, x + 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) listening = !listening;
            else if (button == 1 && mc.isShiftPressed())
                setting.reset();
        } else {
            listening = false;
        }
    }

    public void onKey(int key, int action) {
        if (!listening || action != GLFW.GLFW_PRESS) return;

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            setting.setKey(GLFW.GLFW_KEY_UNKNOWN);
        } else {
            for (KeyBinding bind : mc.options.allKeys) {
                boolean keyAlreadyBound = bind.matchesKey(new KeyInput(key, 0, 0));
                boolean allowMultiple = ClientSettingsFrame.guiSettings.multipleKeybinds.isEnabled();

                if (keyAlreadyBound && !allowMultiple) {
                    ChatUtil.warn("esa tecla ya está cogida por "
                            + Formatting.GREEN + "\"" + I18n.translate(bind.getId()) + "\"");
                    listening = false;
                    return;
                }
            }
            setting.setKey(key);
        }

        listening = false;
    }


    public boolean isFocused() {
        return listening;
    }

    public KeySetting getSetting() {
        return setting;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
    }
}