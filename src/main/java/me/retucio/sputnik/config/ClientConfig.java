package me.retucio.sputnik.config;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.sputnik.*;
import me.retucio.sputnik.module.setting.settings.*;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.panels.FriendsPanel;
import me.retucio.sputnik.util.ChatUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public Map<String, int[]> settingsPanels = new HashMap<>();

    // nombre del frame -> FrameData (que contiene extended, x & y)
    public Map<String, PanelData> extendablePanels = new HashMap<>();

    // id del elemento del hud -> posición (x, y)
    public Map<String, int[]> hudPositions = new HashMap<>();

    // id del elemento del hud -> true: visible / false: no visible
    public Map<String, Boolean> hudVisibilities = new HashMap<>();

    // id del elemento del hud -> ruta de la imagen
    public Map<String, String> hudImagePaths = new HashMap<>();

    // posición de la barra de búsqueda (x, y)
    public int[] searchBarPosition = new int[] { 340, 16 };

    // amigos: uuid -> nombre
    public Map<String, String> friends = new HashMap<>();

    public ClientConfig() {
        EVENT_BUS.subscribe(this);
    }

    @EventListener
    public void onToggleModule(ToggleModuleEvent event) {
        ConfigManager.setModuleState(event.getModule());
    }

    @SuppressWarnings("rawtypes")
    @EventListener
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

    @EventListener
    public void onOpenSettingsPanel(SettingsPanelEvent.Open event) {
        ConfigManager.setPanelPosition(event.getPanel());
    }

    @EventListener
    public void onCloseSettingsPanel(SettingsPanelEvent.Close event) {
        settingsPanels.remove(event.getPanel().getModule().getName());
        ConfigManager.save();
    }

    @EventListener
    public void onMoveSettingsPanel(SettingsPanelEvent.Move event) {
        settingsPanels.replace(event.getPanel().getModule().getName(), new int[]{event.getPanel().getX(), event.getPanel().getY()});
        ConfigManager.save();
    }

    @EventListener
    public void onExtendModulePanel(ModulePanelEvent.Extend event) {
        extendablePanels.put("M", new PanelData(
                ClickGui.INSTANCE.getModulesPanel().getX(),
                ClickGui.INSTANCE.getModulesPanel().getY(),
                ClickGui.INSTANCE.getModulesPanel().extended));
        ConfigManager.save();
    }

    @EventListener
    public void onMoveModulePanel(ModulePanelEvent.Move event) {
        extendablePanels.replace("M", new PanelData(
                ClickGui.INSTANCE.getModulesPanel().getX(),
                ClickGui.INSTANCE.getModulesPanel().getY(),
                ClickGui.INSTANCE.getModulesPanel().extended));
        ConfigManager.save();
    }

    @EventListener
    public void onExtendClientSettingsPanel(ClientSettingsPanelEvent.Extend event) {
        extendablePanels.put("S", new PanelData(
                ClickGui.INSTANCE.getClientSettingsPanel().getX(),
                ClickGui.INSTANCE.getClientSettingsPanel().getY(),
                ClickGui.INSTANCE.getClientSettingsPanel().extended));
        ConfigManager.save();
    }

    @EventListener
    public void onMoveClientSettingsPanel(ClientSettingsPanelEvent.Move event) {
        extendablePanels.replace("S", new PanelData(
                ClickGui.INSTANCE.getClientSettingsPanel().getX(),
                ClickGui.INSTANCE.getClientSettingsPanel().getY(),
                ClickGui.INSTANCE.getClientSettingsPanel().extended));
        ConfigManager.save();
    }

    @EventListener
    public void onExtendFriendsPanel(FriendPanelEvent.Extend event) {
        extendablePanels.put("F", new PanelData(
                ClickGui.INSTANCE.getFriendsPanel().getX(),
                ClickGui.INSTANCE.getFriendsPanel().getY(),
                ClickGui.INSTANCE.getFriendsPanel().extended));
        ConfigManager.save();
    }

    @EventListener
    public void onMoveFriendsPanel(FriendPanelEvent.Move event) {
        extendablePanels.replace("F", new PanelData(
                ClickGui.INSTANCE.getFriendsPanel().getX(),
                ClickGui.INSTANCE.getFriendsPanel().getY(),
                ClickGui.INSTANCE.getFriendsPanel().extended));
        ConfigManager.save();
    }

    @EventListener
    public void onAddFriend(FriendEvent.Add event) {
        String uuid = event.getFriend().getUuid().toString();
        String name = event.getFriend().getName();
        if (!friends.containsKey(uuid)) {
            friends.put(uuid, name);
        }
        ConfigManager.save();
    }
    @EventListener
    public void onRemoveFriend(FriendEvent.Remove event) {
        friends.remove(event.getFriend().getUuid().toString());
        ConfigManager.save();
    }

    public record PanelData(int x, int y, boolean extended) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }
}
