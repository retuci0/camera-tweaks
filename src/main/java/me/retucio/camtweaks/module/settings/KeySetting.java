package me.retucio.camtweaks.module.settings;

import me.retucio.camtweaks.util.KeyUtil;

import java.util.function.Consumer;

public class KeySetting extends Setting {

    private int key;
    private int defaultKey;

    private Consumer<Integer> updateListener;

    public KeySetting(String name, String description, int defaultKey) {
        super(name, description);
        this.key = defaultKey;
        this.defaultKey = defaultKey;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        if (this.key == key) return;
        this.key = key;
        if (updateListener != null) updateListener.accept(key);
    }

    public int getDefaultKey() {
        return defaultKey;
    }

    public void setDefaultKey(int key) {
        this.defaultKey = key;
    }

    public String getKeyName() {
        return KeyUtil.getKeyName(key);
    }

    public void reset() {
        setKey(defaultKey);
    }

    public void onUpdate(Consumer<Integer> listener) {
        this.updateListener = listener;
    }
}
