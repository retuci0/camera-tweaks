package me.retucio.sputnik.config;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.sputnik.*;
import me.retucio.sputnik.module.setting.settings.*;
import me.retucio.sputnik.ui.screen.ClickGUI;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;
import static me.retucio.sputnik.Sputnik.mc;

// clase donde se guardan las configuraciones temporalmente hasta escribirlas en camera_tweaks.json
public class ClientConfig {

    // nombre del módulo -> estado del módulo
    public Map<String, Boolean> moduleStates = new HashMap<>();

    // nombre del ajuste -> valor
    public Map<String, Object> settings = new HashMap<>();

    // nombre del módulo -> posición (x, y)
    public Map<String, int[]> settingsFrames = new HashMap<>();

    // nombre del frame -> FrameData (que contiene extended, x & y)
    public Map<String, FrameData> extendableFrames = new HashMap<>();

    // id del elemento del hud -> posición (x, y)
    public Map<String, int[]> hudPositions = new HashMap<>();

    // id del elemento del hud -> true: visible / false: no visible
    public Map<String, Boolean> hudVisibilities = new HashMap<>();

    // id del elemento del hud -> ruta de la imagen (NEW)
    public Map<String, String> hudImagePaths = new HashMap<>();

    // posición de la barra de búsqueda (x, y)
    public int[] searchBarPosition = new int[]{340, 16};

    public ClientConfig() {
        EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onToggleModule(ToggleModuleEvent event) {
        ConfigManager.setModuleState(event.getModule());
    }

    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public void onUpdateSetting(UpdateSettingEvent event) {
        if (mc == null || !event.shouldSave()) return;

        // guardar ajustes con su respectivo tipo de valor, en formato "nombreMódulo:nombreAjuste"
        Object value = null;
        switch (event.getSetting()) {
            case BooleanSetting b: value = b.getValue(); break;
            case EnumSetting e: value = e.getIndex(); break;
            case KeySetting k: value = k.getValue(); break;
            case NumberSetting n: value = n.getValue(); break;
            case StringSetting s: value = s.getValue(); break;
            case ListSetting l: value = l.getConfigValues(); break;
            case ColorSetting c: value = c.getConfigValue(); break;
            case OptionSetting o: value = o.getDisplayName(); break;
            default: break;
        }

        ConfigManager.setSetting(event.getSetting(), value);
    }

    @SubscribeEvent
    public void onOpenSettingsFrame(SettingsFrameEvent.Open event) {
        ConfigManager.setFramePosition(event.getFrame());
    }

    @SubscribeEvent
    public void onCloseSettingsFrame(SettingsFrameEvent.Close event) {
        settingsFrames.remove(event.getFrame().getModule().getName());
        ConfigManager.save();
    }

    @SubscribeEvent
    public void onMoveSettingsFrame(SettingsFrameEvent.Move event) {
        settingsFrames.replace(event.getFrame().getModule().getName(), new int[]{event.getFrame().getX(), event.getFrame().getY()});
        ConfigManager.save();
    }

    @SubscribeEvent
    public void onExtendModuleFrame(ModuleFrameEvent.Extend event) {
        extendableFrames.put("M", new FrameData(
                ClickGUI.INSTANCE.getModulesFrame().getX(),
                ClickGUI.INSTANCE.getModulesFrame().getY(),
                ClickGUI.INSTANCE.getModulesFrame().extended));
    }

    @SubscribeEvent
    public void onMoveModuleFrame(ModuleFrameEvent.Move event) {
        extendableFrames.replace("M", new FrameData(
                ClickGUI.INSTANCE.getModulesFrame().getX(),
                ClickGUI.INSTANCE.getModulesFrame().getY(),
                ClickGUI.INSTANCE.getModulesFrame().extended));
    }

    @SubscribeEvent
    public void onExtendGUISettingsFrame(GUISettingsFrameEvent.Extend event) {
        extendableFrames.put("S", new FrameData(
                ClickGUI.INSTANCE.getGuiSettingsFrame().getX(),
                ClickGUI.INSTANCE.getGuiSettingsFrame().getY(),
                ClickGUI.INSTANCE.getGuiSettingsFrame().extended));
    }

    @SubscribeEvent
    public void onMoveGUISettingsFrame(GUISettingsFrameEvent.Move event) {
        extendableFrames.replace("S", new FrameData(
                ClickGUI.INSTANCE.getGuiSettingsFrame().getX(),
                ClickGUI.INSTANCE.getGuiSettingsFrame().getY(),
                ClickGUI.INSTANCE.getGuiSettingsFrame().extended));
    }

    public record FrameData(int x, int y, boolean extended) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }
}
