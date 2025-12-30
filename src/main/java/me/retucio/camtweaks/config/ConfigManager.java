package me.retucio.camtweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.settings.*;
import me.retucio.camtweaks.ui.frames.ModuleFrame;
import me.retucio.camtweaks.ui.screen.ClickGUI;
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

    private static boolean loaded = false;
    private static ClientConfig config = null;


    // ------------------ MÉTODOS PRINCIPALES ------------------

    // guardar configuraciones
    public static void save() {
        ensureConfig();

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
            CameraTweaks.LOGGER.info("ajustes guardados");
        } catch (IOException e) {
            CameraTweaks.LOGGER.error("error al guardar ajustes", e);
        }
    }

    // cargar configuraciones
    public static void load() {
        CameraTweaks.LOGGER.info("cargando ajustes...");

        if (!CONFIG_FILE.exists()) {
            ensureConfig();
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            config = GSON.fromJson(reader, ClientConfig.class);
            CameraTweaks.LOGGER.info("ajustes cargados");
        } catch (IOException e) {
            CameraTweaks.LOGGER.error("no se pudieron cargar los ajustes, usando ajustes por defecto", e);
            ensureConfig();
        }
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
        CameraTweaks.LOGGER.info("ajustes aplicados");
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

    public static void setSetting(Setting setting, Object value) {
        ensureConfig();
        config.settings.put(getSettingKey(setting), value);
        save();
    }

    public static void setFramePosition(SettingsFrame frame) {
        ensureConfig();
        config.settingsFrames.put(frame.module.getName(), new int[] {frame.x, frame.y});
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
    public static void applySetting(Module parent, Setting setting) {
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
            default -> CameraTweaks.LOGGER.warn("watafac queseso: {}", setting.getClass().getSimpleName());
        }
    }

    private static void applyModuleStates() {
        ModuleManager.INSTANCE.getModules().forEach(module -> {
            if (config.moduleStates.containsKey(module.getName())) {
                module.setEnabled(config.moduleStates.get(module.getName()));
            }
        });
        CameraTweaks.LOGGER.info("estados de módulos aplicados");
    }

    private static void applyModuleSettings() {
        ModuleManager.INSTANCE.getModules().forEach(module -> {
            module.getSettings().forEach(setting -> applySetting(module, setting));
        });
        CameraTweaks.LOGGER.info("ajustes de módulos aplicados");
    }

    private static void applySettingsFrames() {
        config.settingsFrames.forEach((moduleName, position) -> {
            Module module = ModuleManager.INSTANCE.getModuleByName(moduleName);
            ClickGUI.INSTANCE.openSettingsFrame(module, position[0], position[1]);
        });
        CameraTweaks.LOGGER.info("estados de marcos de ajustes aplicados");
    }

    private static void applySearchBarPosition() {
        if (config.searchBarPosition != null && config.searchBarPosition.length == 2) {
            ClickGUI.INSTANCE.getSearchBar().setX(config.searchBarPosition[0]);
            ClickGUI.INSTANCE.getSearchBar().setY(config.searchBarPosition[1]);
        }
        CameraTweaks.LOGGER.info("posición de la barra de búsqueda aplicada");
    }

    private static void applyGuiSettings() {
        ClientSettingsFrame.guiSettings.setEnabled(true);
        ClientSettingsFrame.guiSettings.getSettings().forEach(setting -> {
            applySetting(ClientSettingsFrame.guiSettings, setting);
        });
        CameraTweaks.LOGGER.info("ajustes del cliente aplicados");
    }

    private static void applyExtendableFrames() {
        applyExtendableFrame(ClickGUI.INSTANCE.getModulesFrame());
        applyExtendableFrame(ClickGUI.INSTANCE.getGuiSettingsFrame());

        CameraTweaks.LOGGER.info("posiciones de marcos extendibles aplicadasa");
        ClickGUI.INSTANCE.refreshListButtons();
    }

    private static void applyExtendableFrame(ModuleFrame frame) {
        ClientConfig.FrameData frameData = config.extendableFrames.get("M");
        if (frameData != null) {
            frame.x = frameData.x();
            frame.y = frameData.y();
            frame.extended = frameData.extended();
        }
    }

    private static void applyExtendableFrame(ClientSettingsFrame frame) {
        ClientConfig.FrameData frameData = config.extendableFrames.get("S");
        if (frameData != null) {
            frame.x = frameData.x();
            frame.y = frameData.y();
            frame.extended = frameData.extended();
        }
    }

    // ajustes de módulos

    private static void applyBooleanSetting(BooleanSetting setting, Object value) {
        if (value instanceof Boolean) {
            setting.setEnabled((Boolean) value);
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
            setting.setKey(((Double) value).intValue());
        } else if (value instanceof Integer) {
            setting.setKey((Integer) value);
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

            setting.setValues(convertedValues);
        }
    }

    private static void applyColorSetting(ColorSetting setting, Object value) {
        if (!(value instanceof Map<?, ?> map)) return;

        Number r = (Number) map.get("r");
        Number g = (Number) map.get("g");
        Number b = (Number) map.get("b");
        Number a = (Number) map.get("a");

        if (r != null && g != null && b != null && a != null)
            setting.setRGB(r.intValue(), g.intValue(), b.intValue(), a.intValue());

        applyOptionalProperty(map, "rb", Boolean.class, setting::setRainbow);
        applyOptionalProperty(map, "rs", Number.class, val -> setting.setRainbowSpeed(val.intValue()));
        applyOptionalProperty(map, "sat", Number.class, val -> setting.setSaturation(val.floatValue()));
        applyOptionalProperty(map, "bri", Number.class, val -> setting.setBrightness(val.floatValue()));
    }

    private static <T> void applyOptionSetting(OptionSetting<T> setting, Object value) {
        if (value instanceof Double) {
            int index = ((Double) value).intValue();
            setting.setValue(setting.getOption(index));
        }
    }

    private static <T> void applyOptionalProperty(Map<?, ?> map, String key, Class<T> type, java.util.function.Consumer<T> setter) {
        Object value = map.get(key);
        if (type.isInstance(value)) {
            setter.accept(type.cast(value));
        }
    }


    // ------------------ MÉTODOS DE AYUDA ------------------

    private static boolean shouldApply() {
        return CameraTweaks.mc != null && ModuleManager.INSTANCE != null && config != null;
    }


    private static void ensureConfig() {
        if (config == null) config = new ClientConfig();
    }

    private static String getSettingKey(Setting setting) {
        return setting.getModule().getName() + ":" + setting.getName();
    }

    public static boolean hasLoaded() {
        return loaded;
    }
}
