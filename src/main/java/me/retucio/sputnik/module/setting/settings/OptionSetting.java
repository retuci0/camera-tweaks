package me.retucio.sputnik.module.setting.settings;

import me.retucio.sputnik.module.setting.Setting;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OptionSetting<T> extends Setting<T> {

    private final List<T> options;
    private Map<T, String> displayNames;

    public OptionSetting(String name, String description, List<T> options, T defaultOption) {
        super(name, description, defaultOption);
        this.options = options;
    }

    public OptionSetting(String name, String description, List<T> options, T defaultOption, Map<T, String> displayNames) {
        super(name, description, defaultOption);
        this.options = options;
        this.displayNames = displayNames;
    }

    public void setValue(T option) {
        if (options.contains(option)) super.setValue(option);
    }

    public void setValueByName(String name) {
        for (T opt : options) {
            if (displayNames != null && !displayNames.isEmpty()) {
                if (displayNames.get(opt).equals(name)) {
                    setValue(opt);
                    return;
                }
            } else if (opt.toString().equals(name)) {
                setValue(opt);
                return;
            }
        }
    }

    public List<T> getOptions() {
        return options;
    }

    public T getOption(int index) {
        return options.get(index);
    }

    public int getSize() {
        return options.size();
    }

    public int getIndex() {
        int index = 0;
        for (T option : options) {
            if (this.value == option) break;
            index += 1;
        }
        return index;
    }

    public void setIndex(int index) {
        setValue(getOption(index));
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

    public Map<T, String> getDisplayNames() {
        return displayNames;
    }

    public String getDisplayName(T key) {
        if (displayNames != null) return displayNames.get(key);
        return key.toString();
    }

    public String getDisplayName() {
        return getDisplayName(value);
    }
}
