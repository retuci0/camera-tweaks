package me.retucio.camtweaks.config;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.camtweaks.*;
import me.retucio.camtweaks.module.settings.*;
import me.retucio.camtweaks.ui.screen.ClickGUI;

import java.util.HashMap;
import java.util.Map;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;
import static me.retucio.camtweaks.CameraTweaks.mc;

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
            case BooleanSetting b: value = b.isEnabled(); break;
            case EnumSetting e: value = e.getIndex(); break;
            case KeySetting k: value = k.getKey(); break;
            case NumberSetting n: value = n.getValue(); break;
            case StringSetting s: value = s.getValue(); break;
            case ListSetting l: value = l.getValues(); break;
            default: break;
        }

        ConfigManager.setSetting(event.getSetting(), value);
    }

    @SubscribeEvent
    public void onOpenSettingsFrame(SettingsFrameEvent.Open event) {
        if (settingsFrames.containsKey(event.getFrame().module.getName())) return;
        ConfigManager.setFramePosition(event.getFrame());
    }

    @SubscribeEvent
    public void onCloseSettingsFrame(SettingsFrameEvent.Close event) {
        settingsFrames.remove(event.getFrame().module.getName());
        ConfigManager.save();
    }

    @SubscribeEvent
    public void onMoveSettingsFrame(SettingsFrameEvent.Move event) {
        settingsFrames.replace(event.getFrame().module.getName(), new int[]{event.getFrame().x, event.getFrame().y});
        ConfigManager.save();
    }

    @SubscribeEvent
    public void onExtendModuleFrame(ModuleFrameEvent.Extend event) {
        extendableFrames.put("M", new FrameData(
                ClickGUI.INSTANCE.getModulesFrame().x,
                ClickGUI.INSTANCE.getModulesFrame().y,
                ClickGUI.INSTANCE.getModulesFrame().extended));
    }

    @SubscribeEvent
    public void onMoveModuleFrame(ModuleFrameEvent.Move event) {
        extendableFrames.replace("M", new FrameData(
                ClickGUI.INSTANCE.getModulesFrame().x,
                ClickGUI.INSTANCE.getModulesFrame().y,
                ClickGUI.INSTANCE.getModulesFrame().extended));
    }

    @SubscribeEvent
    public void onExtendGUISettingsFrame(GUISettingsFrameEvent.Extend event) {
        extendableFrames.put("S", new FrameData(
                ClickGUI.INSTANCE.getGuiSettingsFrame().x,
                ClickGUI.INSTANCE.getGuiSettingsFrame().y,
                ClickGUI.INSTANCE.getGuiSettingsFrame().extended));
    }

    @SubscribeEvent
    public void onMoveGUISettingsFrame(GUISettingsFrameEvent.Move event) {
        extendableFrames.replace("S", new FrameData(
                ClickGUI.INSTANCE.getGuiSettingsFrame().x,
                ClickGUI.INSTANCE.getGuiSettingsFrame().y,
                ClickGUI.INSTANCE.getGuiSettingsFrame().extended));
    }

    public record FrameData(int x, int y, boolean extended) {}
}
