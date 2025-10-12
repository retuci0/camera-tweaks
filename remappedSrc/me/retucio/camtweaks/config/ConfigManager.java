package me.retucio.camtweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.settings.*;
import me.retucio.camtweaks.ui.ClickGUI;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.ui.frames.SettingsFrame;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// se ocupa de guardar, cargar y aplicar ajustes
// pues no me ha dado dolores de cabeza el coso este con los NPE de los cojones...
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("camera_tweaks.json");

    private static ClientConfig config = null;

    // guardar configuraciones
    public static void save() {
        ensureConfig();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
            CameraTweaks.LOGGER.info("ajustes guardados");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // cargar configuraciones
    public static void load() {
        CameraTweaks.LOGGER.info("ajustes cargados");
        if (!CONFIG_FILE.exists()) {
            ensureConfig();
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            config = GSON.fromJson(reader, ClientConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            ensureConfig();
        }
    }

    // aplicar configuraciones cargadas
    public static void applyConfig() {
        if (CameraTweaks.mc == null || ModuleManager.INSTANCE == null || config == null)
            return;

        ModuleManager.INSTANCE.getModules().forEach(module -> {
            if (config.moduleStates.containsKey(module.getName()))
                module.setEnabled(config.moduleStates.get(module.getName()));  // estados

            module.getSettings().forEach(setting -> applySetting(module, setting));

            if (config.settingsFrames.containsKey(module.getName())) {
                int[] position = config.settingsFrames.get(module.getName());
                ClickGUI.INSTANCE.openSettingsFrame(module, position[0], position[1]);
            }
        });

        ClientSettingsFrame.guiSettings.setEnabled(true);
        ClientSettingsFrame.guiSettings.getSettings().forEach(setting -> applySetting(ClientSettingsFrame.guiSettings, setting));

        CameraTweaks.LOGGER.info("módulos encendidos cargados");
        CameraTweaks.LOGGER.info("ajustes de módulos cargados");

        ClientConfig.FrameData moduleFrameData = config.extendableFrames.get("M");
        if (moduleFrameData != null) {
            ClickGUI.INSTANCE.getModulesFrame().x = moduleFrameData.x();
            ClickGUI.INSTANCE.getModulesFrame().y = moduleFrameData.y();
            ClickGUI.INSTANCE.getModulesFrame().extended = moduleFrameData.extended();
        }

        ClientConfig.FrameData guiSettingsData = config.extendableFrames.get("S");
        if (guiSettingsData != null) {
            ClickGUI.INSTANCE.getGuiSettingsFrame().x = guiSettingsData.x();
            ClickGUI.INSTANCE.getGuiSettingsFrame().y = guiSettingsData.y();
            ClickGUI.INSTANCE.getGuiSettingsFrame().extended = guiSettingsData.extended();
        }

        CameraTweaks.LOGGER.info("posiciones de frames cargadas");
        ClickGUI.INSTANCE.refreshListButtons();
    }

    public static ClientConfig getConfig() {
        return config;
    }

    public static void setModuleState(Module module) {
        ensureConfig();
        config.moduleStates.put(module.getName(), module.isEnabled());
        save();
    }

    public static void setSetting(AbstractSetting setting, Object value) {
        ensureConfig();
        config.settings.put(setting.getModule().getName() + ":" + setting.getName(), value);
        save();
    }

    public static void setFramePosition(SettingsFrame frame) {
        ensureConfig();
        config.settingsFrames.put(frame.module.getName(), new int[]{frame.x, frame.y});
        ConfigManager.save();
    }

    public static void setExtendableFrame(String key, ClientConfig.FrameData data) {
        ensureConfig();
        config.extendableFrames.put(key, data);
        save();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void applySetting(Module parent, AbstractSetting setting) {
        String id = parent.getName() + ":" + setting.getName();
        if (config.settings.containsKey(id)) {
            Object value = config.settings.get(id);

            switch (setting) {
                case BooleanSetting b -> b.setEnabled((boolean) value);
                case EnumSetting e -> e.setIndex(value instanceof Double val ? val.intValue() : (int) value);
                case NumberSetting n -> n.setValue((double) value);
                case StringSetting s -> s.setValue((String) value);
                case KeySetting k -> k.setKey(value instanceof Double val ? val.intValue() : (int) value);
                case ListSetting l -> {
                    if (value instanceof Map<?, ?> map) {
                        Map<Object, Boolean> converted = new HashMap<>();
                        for (Object option : l.getOptions()) {
                            Object key = String.valueOf(option);
                            Object bv = map.get(key);
                            converted.put(option, bv instanceof Boolean b && b);
                        }
                        l.setValues(converted);
                    }
                } default -> {}
            }
        }
    }

    private static void ensureConfig() {
        if (config == null) config = new ClientConfig();
    }
}
