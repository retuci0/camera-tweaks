package me.retucio.camtweaks.module.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OptionSetting<T> extends Setting{

    List<T> options = new ArrayList<>();
    Map<T, String> displayNames;
    T option;
    T defaultOption;

    private Consumer<T> updateListener;

    public OptionSetting(String name, String description, List<T> options, T defaultOption) {
        super(name, description);
        this.options = options;
        this.option = defaultOption;
        this.defaultOption = defaultOption;
    }

    public OptionSetting(String name, String description, List<T> options, T defaultOption, Map<T, String> displayNames) {
        super(name, description);
        this.options = options;
        this.option = defaultOption;
        this.defaultOption = defaultOption;
        this.displayNames = displayNames;
    }

    public T getValue() {
        return option;
    }

    public void setValue(T option) {
        if (options.contains(option)) this.option = option;
        fireUpdateEvent();
        if (updateListener != null) updateListener.accept(option);
    }

    public List<T> getOptions() {
        return options;
    }

    public T getOption(int index) {
        return options.get(index);
    }

    public T getDefaultValue() {
        return defaultOption;
    }

    public int getSize() {
        return options.size();
    }

    public int getIndex() {
        int index = 0;
        for (T option : options) {
            if (this.option == option) break;
            index += 1;
        }
        return index;
    }

    public boolean is(T option) {
        return getValue() == option;
    }

    public int getIndex(T option1) {
        int index = 0;
        for (T option : options) {
            if (option1 == option) break;
            index += 1;
        }
        return index;
    }

    public void reset() {
        setValue(defaultOption);
    }

    public void onUpdate(Consumer<T> listener) {
        this.updateListener = listener;
        if (updateListener != null) updateListener.accept(option);
    }

    public Map<T, String> getDisplayNames() {
        return displayNames;
    }

    public String getDisplayName(T key) {
        if (displayNames != null) return displayNames.get(key);
        return key.toString();
    }
}
