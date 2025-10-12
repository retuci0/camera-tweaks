package me.retucio.camtweaks.event.events.camtweaks;

import me.retucio.camtweaks.event.Event;
import me.retucio.camtweaks.module.Module;

// se genera cada que se enciende o apaga un módulo
public class ToggleModuleEvent extends Event {

    private final Module module;

    public ToggleModuleEvent(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}
