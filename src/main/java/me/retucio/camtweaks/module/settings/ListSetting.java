package me.retucio.camtweaks.module.settings;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.UpdateSettingEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ListSetting<T> extends AbstractSetting {

    private final List<T> options;
    private Map<T, Boolean> values;
    private Map<T, Boolean> defaultValues;
    private Map<T, String> displayNames = null;

    private Consumer<Map<T, Boolean>> updateListener;

    public ListSetting(String name, String description, List<T> options, Map<T, Boolean> initialValues) {
        super(name, description);
        this.options = options;
        this.defaultValues = new HashMap<>();

        for (T option : options) {
            boolean enabled = initialValues != null && initialValues.getOrDefault(option, false);
            defaultValues.put(option, enabled);
        }
        this.values = new HashMap<>(defaultValues);
    }

    public ListSetting(String name, String description, List<T> options, Map<T, Boolean> initialValues, Map<T, String> displayNames) {
        this(name, description, options, initialValues);
        this.displayNames = displayNames;
    }

    public boolean isEnabled(T option) {
        return values.getOrDefault(option, false);
    }

    public void setEnabled(T option, boolean enabled) {
        if (values.containsKey(option) && values.get(option) != enabled) {
            values.put(option, enabled);
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(new HashMap<>(values));
        }
    }

    public void toggle(T option) {
        setEnabled(option, !isEnabled(option));
    }

    public Map<T, Boolean> getValues() {
        return new HashMap<>(values);
    }

    public void setValues(Map<T, Boolean> values) {
        this.values = new HashMap<>(values);
    }

    public Map<T, Boolean> getDefaultValues() {
        return new HashMap<>(defaultValues);
    }

    public void setDefaultValues(Map<T, Boolean> values) {
        defaultValues = values;
    }

    public void reset() {
        values.clear();
        values.putAll(defaultValues);
        fireUpdateEvent();
        if (updateListener != null) updateListener.accept(new HashMap<>(values));
    }

    public void setAll(boolean enabled) {
        for (T option : options) values.put(option, enabled);
    }

    public void onUpdate(Consumer<Map<T, Boolean>> listener) {
        this.updateListener = listener;
        if (updateListener != null) updateListener.accept(new HashMap<>(values));
    }

    public List<T> getOptions() {
        return options;
    }

    public Map<T, String> getDisplayNames() {
        return displayNames;
    }

    public String getDisplayName(T key) {
        if (displayNames != null) return displayNames.get(key);
        return key.toString();
    }
}