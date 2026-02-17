package me.retucio.sputnik.ui.widgets.buttons.settings;

import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;


// botón para configurar la tecla asignada a un módulo. comienza a escuchar al hacerle clic.
public class BindButton extends SettingButton<KeySetting> {

    private boolean listening = false;

    public BindButton(KeySetting setting, SettingsFrame parent, int offset) {
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
        String label = setting.getName() + ": " + (listening ? "..." : setting.getKeyName());
        ctx.drawText(mc.textRenderer, label, x + 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovered = isHovered(mouseX, mouseY);

        if (hovered && ClickGUI.INSTANCE.trySelect(this)) {
            if (listening) {
                List<KeyBinding> keys = new ArrayList<>(List.of(mc.options.allKeys));
                keys.removeAll(List.of(mc.options.debugKeys));
                boolean allowMultiple = ClientSettingsFrame.guiSettings.multipleKeybinds.getValue();

                for (KeyBinding bind : keys) {
                    if (bind.matchesKey(new KeyInput(button, 0, 0))) {
                        if (!allowMultiple) {
                            ChatUtil.warn("esa tecla ya está cogida por "
                                    + Formatting.GREEN + "\"" + I18n.translate(bind.getId()) + "\"");
                            listening = false;
                            return;
                        }
                    }
                }

                setting.setValue(button);
                listening = false;
            } else {
                if (button == 0) {
                    listening = true;
                } else if (button == 1 && KeyUtil.isShiftDown()) {
                    setting.reset();
                }
            }
        } else {
            if (listening) {
                listening = false;
            }
        }
    }

    public void onKey(int key, int action) {
        if (!listening || action != GLFW.GLFW_PRESS) return;

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            setting.setValue(GLFW.GLFW_KEY_UNKNOWN);
        } else {
            List<KeyBinding> keys = new ArrayList<>(List.of(mc.options.allKeys));
            keys.removeAll(List.of(mc.options.debugKeys));
            for (KeyBinding bind : keys) {
                // no permitir usar la misma tecla para varias acciones, si el ajuste para esto está activado
                boolean keyAlreadyBound = bind.matchesKey(new KeyInput(key, 0, 0));
                boolean allowMultiple = ClientSettingsFrame.guiSettings.multipleKeybinds.getValue();

                if (keyAlreadyBound && !allowMultiple) {
                    ChatUtil.warn("esa tecla ya está cogida por "
                            + Formatting.GREEN + "\"" + I18n.translate(bind.getId()) + "\"");
                    listening = false;
                    return;
                }
            }
            setting.setValue(key);
        }

        listening = false;
    }

    public boolean isFocused() {
        return listening;
    }
}