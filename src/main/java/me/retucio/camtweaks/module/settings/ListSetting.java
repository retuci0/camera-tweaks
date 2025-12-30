package me.retucio.camtweaks.module.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ListSetting<T> extends Setting {

    private List<T> options;
    private Map<T, Boolean> values;
    private Map<T, Boolean> defaultValues;
    private Map<T, String> displayNames = null;

    private Consumer<Map<T, Boolean>> updateListener;

    public ListSetting(String name, String description, List<T> options, Map<T, Boolean> initialValues) {
        super(name, description);
        this.options = new ArrayList<>(options);
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

    public Map<String, Boolean> getConfigValues() {
        Map<String, Boolean> configValues = new HashMap<>();
        for (int i = 0; i < values.size(); i++)
            configValues.put(getDisplayName(options.get(i)), values.get(options.get(i)));
        return configValues;
    }

    public void addOption(T option, boolean value, String displayName) {
        if (!(this.options instanceof ArrayList))
            this.options = new ArrayList<>(this.options);

        this.options.add(option);
        this.values.put(option, value);
        this.defaultValues.put(option, value);

        if (this.displayNames != null)
            this.displayNames.put(option, displayName);
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