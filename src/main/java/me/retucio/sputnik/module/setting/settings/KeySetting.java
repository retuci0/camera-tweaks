package me.retucio.sputnik.module.setting.settings;

import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.setting.Setting;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.KeyUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

public class KeySetting extends Setting<Integer> {

    public KeySetting(String name, String description, int defaultKey) {
        super(name, description, defaultKey);
        this.value = defaultKey;
        this.defaultValue = defaultKey;
    }

    @Override
    public void setValue(Integer value) {
        if (this.value.equals(value)) return;

        if (value == GLFW.GLFW_KEY_ESCAPE) {
            this.value = GLFW.GLFW_KEY_UNKNOWN;  // asignar la tecla ESC a deshabilitar la tecla
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(value);
            return;
        }

        if (ModuleManager.INSTANCE != null) {
            for (Module module : ModuleManager.INSTANCE.getModules())
                if (module.getKey() == value && !ClientSettingsFrame.guiSettings.multipleKeybinds.getValue() && value != -1) {
                    module.setKey(-1);
                    ChatUtil.info("tecla del módulo " + module.getName() + " restablecida");
                }
        }

        super.setValue(value);
    }

    public String getKeyName() {
        return KeyUtil.getKeyName(value);
    }
}
