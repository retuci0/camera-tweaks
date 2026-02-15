package me.retucio.sputnik.event.sputnik;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.module.Module;

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
