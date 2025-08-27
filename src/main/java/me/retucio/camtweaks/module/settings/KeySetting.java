package me.retucio.camtweaks.module.settings;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.UpdateSettingEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.ui.frames.ClickGUISettingsFrame;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.KeyUtil;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class KeySetting extends Setting {

    private int key;
    private int defaultKey;

    private Consumer<Integer> updateListener;

    public KeySetting(String name, String description, int defaultKey) {
        super(name, description);
        this.key = defaultKey;
        this.defaultKey = defaultKey;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        if (this.key == key) return;

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.key = GLFW.GLFW_KEY_UNKNOWN;  // asignar la tecla ESC a deshabilitar la tecla
            CameraTweaks.EVENT_BUS.post(new UpdateSettingEvent(this));
            if (updateListener != null) updateListener.accept(key);
            return;
        }

        if (ModuleManager.INSTANCE != null) {
            for (Module module : ModuleManager.INSTANCE.getModules())
                if (module.getKey() == key && !ClickGUISettingsFrame.guiSettings.multipleKeybinds.isEnabled() && key != -1) {
                    module.setKey(-1);
                    ChatUtil.info("tecla del módulo " + module.getName() + " restablecida");
                }
        }
        this.key = key;

        CameraTweaks.EVENT_BUS.post(new UpdateSettingEvent(this));
        if (updateListener != null) updateListener.accept(key);
    }

    public int getDefaultKey() {
        return defaultKey;
    }

    public void setDefaultKey(int key) {
        this.defaultKey = key;
    }

    public String getKeyName() {
        return KeyUtil.getKeyName(key);
    }

    public void reset() {
        setKey(defaultKey);
    }

    public void onUpdate(Consumer<Integer> listener) {
        this.updateListener = listener;
    }
}
