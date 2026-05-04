package me.retucio.sputnik.ui.widgets.buttons.settings;

import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;


// botón para configurar la tecla asignada a un módulo. comienza a escuchar al hacerle clic.
public class BindButton extends SettingButton<KeySetting> {

    private boolean listening = false;

    public BindButton(KeySetting setting, SettingsPanel parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        // fondo
        int bgColor = isHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();
        gui.fill(x, y, x + w, y + h, bgColor);

        // texto
        String label = setting.getName() + ": " + (listening ? "..." : setting.getKeyName());
        gui.text(mc.font, label, x + 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovered = isHovered(mouseX, mouseY);

        if (hovered && ClickGui.INSTANCE.trySelect(this)) {
            if (listening) {
                List<KeyMapping> keys = new ArrayList<>(List.of(mc.options.keyMappings));
                keys.removeAll(List.of(mc.options.debugKeys));
                boolean allowMultiple = ClientSettingsPanel.clientSettings.multipleKeybinds.getValue();

                for (KeyMapping bind : keys) {
                    if (bind.matches(new KeyEvent(button, 0, 0))) {
                        if (!allowMultiple) {
                            ChatUtil.warn("esa tecla ya está cogida por "
                                    + ChatFormatting.GREEN + "\"" + I18n.get(bind.getName()) + "\"");
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
            List<KeyMapping> keys = new ArrayList<>(List.of(mc.options.keyMappings));
            keys.removeAll(List.of(mc.options.debugKeys));
            for (KeyMapping bind : keys) {
                // no permitir usar la misma tecla para varias acciones, si el ajuste para esto está activado
                boolean keyAlreadyBound = bind.matches(new KeyEvent(key, 0, 0));
                boolean allowMultiple = ClientSettingsPanel.clientSettings.multipleKeybinds.getValue();

                if (keyAlreadyBound && !allowMultiple) {
                    ChatUtil.warn("esa tecla ya está cogida por "
                            + ChatFormatting.GREEN + "\"" + I18n.get(bind.getName()) + "\"");
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