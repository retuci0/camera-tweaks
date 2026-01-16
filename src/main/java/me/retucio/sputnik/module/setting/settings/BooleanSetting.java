package me.retucio.sputnik.module.setting.settings;

import me.retucio.sputnik.module.setting.Setting;

// ajuste booleano, es decir, o se encuentra encencido o apagado (similar a un interruptor)
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public void toggle() {
        setValue(!value);
        if (updateListener != null) updateListener.accept(value);
    }
}