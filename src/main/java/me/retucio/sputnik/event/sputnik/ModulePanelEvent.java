package me.retucio.sputnik.event.sputnik;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.ui.widgets.panels.ModulePanel;

// se genera cada que se interactúa con un panel de módulos
public class ModulePanelEvent extends Event {

    protected ModulePanel panel;

    public ModulePanel getPanel() {
        return panel;
    }

    // al extenderlo / contraerlo
    public static class Extend extends ModulePanelEvent {
        public Extend(ModulePanel panel) {
            this.panel = panel;
        }
    }

    // al cambiarlo de posición
    public static class Move extends ModulePanelEvent {
        public Move(ModulePanel panel) {
            this.panel = panel;
        }
    }
}