package me.retucio.sputnik.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.setting.*;
import me.retucio.sputnik.module.setting.settings.*;
import me.retucio.sputnik.ui.widgets.Frame;
import me.retucio.sputnik.ui.widgets.frames.ModuleFrame;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// se ocupa de guardar, cargar y aplicar ajustes
public class ConfigManager {

    private static final File CONFIG_FILE = new File("sputnik.dat");
    private static final File LEGACY_JSON_FILE = new File("sputnik.json");

    private static boolean loaded = false;
    private static ClientConfig config = null;


    // ------------------ MÉTODOS PRINCIPALES ------------------

    // guardar configuraciones
    public static void save() {
        if (!loaded) return;
        ensureConfig();

        try {
            BinarySerializer.writeConfig(config, CONFIG_FILE);
            Sputnik.LOGGER.info("ajustes guardados");
        } catch (IOException e) {
            Sputnik.LOGGER.error("error al guardar ajustes", e);
        }
    }

    public static void load() {
        Sputnik.LOGGER.info("cargando ajustes...");

        if (CONFIG_FILE.exists()) {
            try {
                config = BinarySerializer.readConfig(CONFIG_FILE);
                Sputnik.LOGGER.info("ajustes cargados");
                return;
            } catch (IOException e) {
                Sputnik.LOGGER.error("error cargando ajustes, intentando json", e);
            }
        }

        // Fallback to legacy JSON
        if (LEGACY_JSON_FILE.exists()) {
            try (FileReader reader = new FileReader(LEGACY_JSON_FILE)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                config = gson.fromJson(reader, ClientConfig.class);
                Sputnik.LOGGER.info("ajustes json cargados (migración)");

                save();
                return;
            } catch (IOException e) {
                Sputnik.LOGGER.error("error cargando json, usando defaults", e);
            }
        }

        // crear nuevo config
        ensureConfig();
        save();
    }

    // aplicar configuraciones cargadas
    public static void applyConfig() {
        if (!shouldApply()) return;

        applyModuleStates();
        applyModuleSettings();
        applySettingsFrames();
        applySearchBarPosition();
        applyGuiSettings();
        applyExtendableFrames();

        loaded = true;
        Sputnik.LOGGER.info("ajustes aplicados");
    }

    // obtener configuración actual
    public static ClientConfig getConfig() {
        ensureConfig();
        return config;
    }


    // ------------------ GUARDAR VALORES INDIVIDUALES EN LA CONFIG. ------------------

    public static void setModuleState(Module module) {
        ensureConfig();
        config.moduleStates.put(module.getName(), module.isEnabled());
        save();
    }

    public static void setSetting(Setting<?> setting, Object value) {
        ensureConfig();
        config.settings.put(getSettingKey(setting), value);
        save();
    }

    public static void setFramePosition(SettingsFrame frame) {
        ensureConfig();
        if (!config.settingsFrames.containsKey(frame.getModule().getName()))
            config.settingsFrames.put(frame.getModule().getName(), new int[] {frame.getX(), frame.getY()});
        else
            config.settingsFrames.replace(frame.getModule().getName(), new int[] {frame.getX(), frame.getY()});
        save();
    }

    public static void setExtendableFrame(String key, ClientConfig.FrameData data) {
        ensureConfig();
        config.extendableFrames.put(key, data);
        save();
    }

    public static void setHudPosition(String id, int x, int y) {
        ensureConfig();
        config.hudPositions.put(id, new int[] {x, y});
        save();
    }

    public static void setHudVisibility(String id, Boolean visible) {
        ensureConfig();
        config.hudVisibilities.put(id, visible);
        save();
    }

    public static void setSearchBarPosition(int x, int y) {
        ensureConfig();
        config.searchBarPosition = new int[] {x, y};
        save();
    }


    // ------------------ APLICAR AJUSTES ------------------

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void applySetting(Module parent, Setting<?> setting) {
        String key = getSettingKey(setting);

        if (!config.settings.containsKey(key)) return;

        Object value = config.settings.get(key);

        switch (setting) {
            case BooleanSetting booleanSetting -> applyBooleanSetting(booleanSetting, value);
            case EnumSetting enumSetting -> applyEnumSetting(enumSetting, value);
            case NumberSetting numberSetting -> applyNumberSetting(numberSetting, value);
            case StringSetting stringSetting -> applyStringSetting(stringSetting, value);
            case KeySetting keySetting -> applyKeySetting(keySetting, value);
            case ListSetting listSetting -> applyListSetting(listSetting, value);
            case ColorSetting colorSetting -> applyColorSetting(colorSetting, value);
            case OptionSetting optionSetting -> applyOptionSetting(optionSetting, value);
            default -> Sputnik.LOGGER.warn("watafac queseso: {}", setting.getClass().getSimpleName());
        }
    }

    private static void applyModuleStates() {
        ModuleManager.INSTANCE.getModules().forEach(module -> {
            if (config.moduleStates.containsKey(module.getName())) {
                module.setEnabled(config.moduleStates.get(module.getName()));
            }
        });
        Sputnik.LOGGER.info("estados de módulos aplicados");
    }

    private static void applyModuleSettings() {
        ModuleManager.INSTANCE.getModules().forEach(module -> {
            module.getSettings().forEach(setting -> applySetting(module, setting));
        });
        Sputnik.LOGGER.info("ajustes de módulos aplicados");
    }

    private static void applySettingsFrames() {
        config.settingsFrames.forEach((moduleName, position) -> {
            Module module = ModuleManager.INSTANCE.getModuleByName(moduleName);

            if (module == null) {
                Sputnik.LOGGER.warn("\"null\" como módulo (\"{}\")", moduleName);
                return;
            }

            ClickGUI.INSTANCE.openSettingsFrame(module, position[0], position[1]);
        });

        Sputnik.LOGGER.info("estados de marcos de ajustes aplicados");
    }

    private static void applySearchBarPosition() {
        if (config.searchBarPosition != null && config.searchBarPosition.length == 2) {
            ClickGUI.INSTANCE.getSearchBar().setX(config.searchBarPosition[0]);
            ClickGUI.INSTANCE.getSearchBar().setY(config.searchBarPosition[1]);
        }
        Sputnik.LOGGER.info("posición de la barra de búsqueda aplicada");
    }

    private static void applyGuiSettings() {
        ClientSettingsFrame.guiSettings.setEnabled(true);
        ClientSettingsFrame.guiSettings.getSettings().forEach(setting -> {
            applySetting(ClientSettingsFrame.guiSettings, setting);
        });
        Sputnik.LOGGER.info("ajustes del cliente aplicados");
    }

    private static void applyExtendableFrames() {
        applyExtendableFrame(ClickGUI.INSTANCE.getModulesFrame());
        applyExtendableFrame(ClickGUI.INSTANCE.getGuiSettingsFrame());

        Sputnik.LOGGER.info("posiciones de marcos extendibles aplicadas");
        ClickGUI.INSTANCE.refreshListButtons();
    }

    private static void applyExtendableFrame(Frame<?> frame) {
        ClientConfig.FrameData frameData = null;
        if (frame instanceof ModuleFrame)
            frameData = config.extendableFrames.get("M");

        if (frame instanceof ClientSettingsFrame)
            frameData = config.extendableFrames.get("S");

        if (frameData != null) {
            frame.setX(frameData.x());
            frame.setY(frameData.y());

            if (frame instanceof ModuleFrame mFrame)
                mFrame.extended = frameData.extended();
            if (frame instanceof ClientSettingsFrame sFrame)
                sFrame.extended = frameData.extended();
        }
    }


    // ajustes de módulos

    private static void applyBooleanSetting(BooleanSetting setting, Object value) {
        if (value instanceof Boolean) {
            setting.setValue((Boolean) value);
        }
    }

    private static <T extends Enum<T>> void applyEnumSetting(EnumSetting<T> setting, Object value) {
        if (value instanceof Double) {
            setting.setIndex(((Double) value).intValue());
        } else if (value instanceof Integer) {
            setting.setIndex((Integer) value);
        }
    }

    private static void applyNumberSetting(NumberSetting setting, Object value) {
        if (value instanceof Double) {
            setting.setValue((Double) value);
        }
    }

    private static void applyStringSetting(StringSetting setting, Object value) {
        if (value instanceof String) {
            setting.setValue((String) value);
        }
    }

    private static void applyKeySetting(KeySetting setting, Object value) {
        if (value instanceof Double) {
            setting.setValue(((Double) value).intValue());
        } else if (value instanceof Integer) {
            setting.setValue((Integer) value);
        }
    }

    private static <T> void applyListSetting(ListSetting<T> setting, Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<T, Boolean> convertedValues = new HashMap<>();

            for (T option : setting.getOptions()) {
                String key = setting.getDisplayName(option);
                Object booleanValue = map.get(key);
                convertedValues.put(option, booleanValue instanceof Boolean && (Boolean) booleanValue);
            }

            setting.setValue(convertedValues);
        }
    }

    private static void applyColorSetting(ColorSetting setting, Object value) {
        if (!(value instanceof Map<?, ?> map)) return;

        Number r = (Number) map.get("r");
        Number g = (Number) map.get("g");
        Number b = (Number) map.get("b");
        Number a = (Number) map.get("a");

        if (r != null && g != null && b != null && a != null) {
            setting.setRGB(
                    r.intValue(),
                    g.intValue(),
                    b.intValue(),
                    a.intValue()
            );
        }

        applyOptionalProperty(map, "rb", Boolean.class, setting::setRainbow);
        applyOptionalProperty(map, "rs", Number.class, val -> setting.setRainbowSpeed(val.intValue()));
        applyOptionalProperty(map, "sat", Number.class, val -> setting.setSaturation(val.floatValue()));
        applyOptionalProperty(map, "bri", Number.class, val -> setting.setBrightness(val.floatValue()));
    }

    private static <T> void applyOptionSetting(OptionSetting<T> setting, Object value) {
        if (value instanceof String name) {
            setting.setValueByName(name);
        }
    }

    private static <T> void applyOptionalProperty(Map<?, ?> map, String key, Class<T> type, Consumer<T> setter) {
        Object value = map.get(key);
        if (type.isInstance(value)) {
            setter.accept(type.cast(value));
        }
    }


    // ------------------ MÉTODOS DE AYUDA ------------------

    private static boolean shouldApply() {
        return Sputnik.mc != null && ModuleManager.INSTANCE != null && config != null;
    }

    private static void ensureConfig() {
        if (config == null) config = new ClientConfig();
    }

    private static String getSettingKey(Setting<?> setting) {
        return setting.getSg().getModule().getName() + ":" + setting.getName();
    }

    public static boolean hasLoaded() {
        return loaded;
    }
}