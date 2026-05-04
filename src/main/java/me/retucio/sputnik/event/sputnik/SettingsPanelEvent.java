package me.retucio.sputnik.event.sputnik;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;

// se genera cada que se interactúa con un marco de ajustes de un módulo
public class SettingsPanelEvent {

    public static class Open extends Event {  // al abrirlo
        private final SettingsPanel panel;
        public Open(SettingsPanel panel) { this.panel = panel; }
        public SettingsPanel getPanel() { return panel; }
    }

    public static class Close extends Event {  // al cerrarlo
        private final SettingsPanel panel;
        public Close(SettingsPanel panel) { this.panel = panel; }
        public SettingsPanel getPanel() { return panel; }
    }

    public static class Move extends Event {  // al cambiarlo de posición
        private final SettingsPanel panel;
        public Move(SettingsPanel panel) { this.panel = panel; }
        public SettingsPanel getPanel() { return panel; }
    }

}