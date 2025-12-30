package me.retucio.camtweaks.module.settings;

import java.util.function.Consumer;

// ajuste booleano, es decir, o se encuentra encencido o apagado (similar a un interruptor)
public class BooleanSetting extends Setting {

    private boolean defaultValue;
    private boolean enabled;

    private Consumer<Boolean> updateListener;

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description);
        this.enabled = defaultValue;
        this.defaultValue = defaultValue;
    }

    public void toggle() {
        setEnabled(!enabled);
        if (updateListener != null) updateListener.accept(enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(enabled);
        }
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean value) {
        defaultValue = value;
    }

    public void reset() {
        setEnabled(defaultValue);
    }

    public void onUpdate(Consumer<Boolean> listener) {
        this.updateListener = listener;
        if (updateListener != null) updateListener.accept(enabled);  // actualizar por primera vez
    }
}
