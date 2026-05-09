package me.retucio.sputnik.module.setting;

import me.retucio.sputnik.module.Module;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SettingGroup implements Iterable<Setting<?>> {

    private Module module;
    private String name;
    private boolean extended;

    final List<Setting<?>> settings = new ArrayList<>(1);

    public SettingGroup(String name, boolean extended) {
        this.name = name;
        this.extended = extended;
    }

    @Override
    public @NotNull Iterator<Setting<?>> iterator() {
        return settings.iterator();
    }

    public <S extends Setting<?>> S add(S setting) {
        setting.setSg(this);
        settings.add(setting);
        return setting;
    }

    @SafeVarargs
    public final <S extends Setting<?>> void addAll(S... ss) {
        for (S s : ss) {
            add(s);
        }
    }

    public Setting<?> getSetting(String name) {
        for (Setting<?> setting : this)
            if (setting.getName().equals(name))
                return setting;

        return null;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }
}
