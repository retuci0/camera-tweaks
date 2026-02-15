package me.retucio.sputnik.event.sputnik;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.module.setting.Setting;

// se genera cada que se cambia el valor de un ajuste
public class UpdateSettingEvent extends Event {

    private final Setting<?> setting;
    private boolean shouldSave;

    public UpdateSettingEvent(Setting<?> setting, boolean shouldSave) {
        this.setting = setting;
        this.shouldSave = shouldSave;
    }

    public Setting<?> getSetting() {
        return setting;
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    public void shouldSave(boolean value) {
        this.shouldSave = value;
    }
}
