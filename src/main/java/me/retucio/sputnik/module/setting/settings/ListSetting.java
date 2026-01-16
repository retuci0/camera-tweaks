package me.retucio.sputnik.module.setting.settings;

import me.retucio.sputnik.module.setting.Setting;

import java.util.*;

public class ListSetting<T> extends Setting<Map<T, Boolean>> {

    private final List<T> options;
    private Map<T, String> displayNames = null;

    public ListSetting(String name, String description, List<T> options, Map<T, Boolean> initialValues) {
        super(name, description, initialValues);
        this.options = new ArrayList<>(options);
        this.defaultValue = new HashMap<>();

        for (T option : options) {
            boolean enabled = initialValues != null && initialValues.getOrDefault(option, false);
            defaultValue.put(option, enabled);
        }

        this.value = new HashMap<>(defaultValue);
    }

    public ListSetting(String name, String description, List<T> options, Map<T, Boolean> initialValues, Map<T, String> displayNames) {
        this(name, description, options, initialValues);
        this.displayNames = displayNames;
    }

    public boolean isEnabled(T option) {
        return value.getOrDefault(option, false);
    }

    public void setEnabled(T option, boolean enabled) {
        if (value.containsKey(option) && value.get(option) != enabled) {
            value.put(option, enabled);
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(new HashMap<>(value));
        }
    }

    public void toggle(T option) {
        setEnabled(option, !isEnabled(option));
    }

    public Map<T, Boolean> getValues() {
        return new HashMap<>(value);
    }

    @Override
    public void setValue(Map<T, Boolean> values) {
        if (!this.value.equals(values)) {
            super.setValue(values);
        }
    }

    public void setDefaultValue(Map<T, Boolean> values) {
        defaultValue.clear();
        defaultValue.putAll(values);
    }

    public Map<String, Boolean> getConfigValues() {
        Map<String, Boolean> configValues = new HashMap<>();
        for (T option : options) {
            configValues.put(getDisplayName(option), value.getOrDefault(option, false));
        }
        return configValues;
    }

    public void addOption(T option, boolean isDefault, String displayName) {
        options.add(option);
        defaultValue.put(option, isDefault);
        value.put(option, isDefault);

        if (this.displayNames != null) {
            this.displayNames.put(option, displayName);
        }
    }

    public void setAll(boolean enabled) {
        for (T option : options) {
            value.put(option, enabled);
        }
        fireUpdateEvent();
        if (updateListener != null) updateListener.accept(new HashMap<>(value));
    }

    public List<T> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public List<T> getEnabledOptions() {
        List<T> enabledOptions = new ArrayList<>();
        for (T option : options) {
            if (isEnabled(option)) {
                enabledOptions.add(option);
            }
        }
        return enabledOptions;
    }

    public Map<T, String> getDisplayNames() {
        return displayNames != null ? new HashMap<>(displayNames) : null;
    }

    public String getDisplayName(T key) {
        if (displayNames != null && displayNames.containsKey(key)) {
            return displayNames.get(key);
        }
        return key != null ? key.toString() : "?";
    }
}