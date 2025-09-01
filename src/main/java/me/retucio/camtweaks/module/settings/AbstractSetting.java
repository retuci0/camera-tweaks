package me.retucio.camtweaks.module.settings;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.UpdateSettingEvent;
import me.retucio.camtweaks.module.Module;

// base para los tipos de ajustes
public abstract class AbstractSetting {

    private final String name;
    private final String description;
    private boolean visible = true;

    private Module module;

    protected AbstractSetting(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public void fireUpdateEvent() {
        CameraTweaks.EVENT_BUS.post(new UpdateSettingEvent(this, module.shouldSaveSettings()));
    }
}
