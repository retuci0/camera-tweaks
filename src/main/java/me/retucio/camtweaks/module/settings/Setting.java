package me.retucio.camtweaks.module.settings;

// base para los tipos de ajustes
public class Setting {

    private final String name;
    private final String description;
    private boolean visible = true;

    protected Setting(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
