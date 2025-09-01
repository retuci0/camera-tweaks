package me.retucio.camtweaks.event.events.camtweaks;

import me.retucio.camtweaks.event.Event;
import me.retucio.camtweaks.module.settings.AbstractSetting;

// se genera cada que se cambia el valor de un ajuste
public class UpdateSettingEvent extends Event {

    private final AbstractSetting setting;
    private boolean shouldSave;

    public UpdateSettingEvent(AbstractSetting setting, boolean shouldSave) {
        this.setting = setting;
        this.shouldSave = shouldSave;
    }

    public AbstractSetting getSetting() {
        return setting;
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    public void shouldSave(boolean value) {
        this.shouldSave = value;
    }
}
