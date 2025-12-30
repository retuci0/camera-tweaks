package me.retucio.camtweaks.event.events.camtweaks;

import me.retucio.camtweaks.event.Event;
import me.retucio.camtweaks.module.settings.Setting;

// se genera cada que se cambia el valor de un ajuste
public class UpdateSettingEvent extends Event {

    private final Setting setting;
    private boolean shouldSave;

    public UpdateSettingEvent(Setting setting, boolean shouldSave) {
        this.setting = setting;
        this.shouldSave = shouldSave;
    }

    public Setting getSetting() {
        return setting;
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    public void shouldSave(boolean value) {
        this.shouldSave = value;
    }
}
