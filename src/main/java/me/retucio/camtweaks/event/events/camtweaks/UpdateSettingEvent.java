package me.retucio.camtweaks.event.events.camtweaks;

import me.retucio.camtweaks.event.Event;
import me.retucio.camtweaks.module.settings.Setting;

// se genera cada que se cambia el valor de un ajuste
public class UpdateSettingEvent extends Event {

    private final Setting setting;

    public UpdateSettingEvent(Setting setting) {
        this.setting = setting;
    }

    public Setting getSetting() {
        return setting;
    }
}
