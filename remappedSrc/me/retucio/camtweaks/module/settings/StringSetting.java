package me.retucio.camtweaks.module.settings;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.UpdateSettingEvent;

import java.util.function.Consumer;

public class StringSetting extends AbstractSetting {

    private String value;
    private String defaultValue;
    private final int maxLength;

    private Consumer<String> updateListener;

    public StringSetting(String name, String description, String defaultValue, int maxLength) {
        super(name, description);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.maxLength = maxLength;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (this.value.equals(value)) return;
        this.value = value.length() > maxLength ? value.substring(0, maxLength) : value;
        fireUpdateEvent();
        if (updateListener != null) updateListener.accept(this.value);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void reset() {
        setValue(defaultValue);
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void onUpdate(Consumer<String> listener) {
        this.updateListener = listener;
        if (updateListener != null) updateListener.accept(value);
    }
}
