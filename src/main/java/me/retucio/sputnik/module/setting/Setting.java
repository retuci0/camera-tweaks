package me.retucio.sputnik.module.setting;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.UpdateSettingEvent;
import me.retucio.sputnik.module.Module;

import java.util.function.Consumer;

// base para los tipos de ajustes
public abstract class Setting<T> {

    protected final String name;
    protected final String description;

    protected T value;
    protected T defaultValue;

    protected Consumer<T> updateListener;

    protected boolean visible = true;
    protected boolean searchMatch = true;

    protected SettingGroup sg;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSearchMatch() {
        return searchMatch;
    }

    public void setSearchMatch(boolean searchMatch) {
        this.searchMatch = searchMatch;
    }

    public String getName() {
        return name.strip();
    }

    public String getTrueName() { return name; }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (this.value != value) {
            this.value = value;

            fireUpdateEvent();
            if (updateListener != null) {
                updateListener.accept(value);
            }
        }
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T value) {
        if (this.defaultValue != value) {
            this.defaultValue = value;
        }
    }

    public void reset() {
        setValue(defaultValue);
    }

    public SettingGroup getSg() {
        return sg;
    }

    public void setSg(SettingGroup sg) {
        this.sg = sg;
    }

    public Module getModule() {
        return this.sg.getModule();
    }

    public void fireUpdateEvent() {
        Sputnik.EVENT_BUS.post(
                new UpdateSettingEvent(this, getModule().shouldSaveSettings()));
    }

    public void onUpdate(Consumer<T> listener) {
        this.updateListener = listener;
        if (updateListener != null) updateListener.accept(value);  // actualizar por primera vez
    }
}
