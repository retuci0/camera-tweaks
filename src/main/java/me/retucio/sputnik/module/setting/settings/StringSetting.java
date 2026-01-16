package me.retucio.sputnik.module.setting.settings;

import me.retucio.sputnik.module.setting.Setting;

import java.util.function.Consumer;

public class StringSetting extends Setting<String> {

    private final int maxLength;

    public StringSetting(String name, String description, String defaultValue, int maxLength) {
        super(name, description, defaultValue);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.maxLength = maxLength;
    }

    public void setValue(String value) {
        if (this.value.equals(value)) return;
        value = value.length() > maxLength ? value.substring(0, maxLength) : value;
        super.setValue(value);
    }

    public int getMaxLength() {
        return maxLength;
    }
}
