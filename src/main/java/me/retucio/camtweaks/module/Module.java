package me.retucio.camtweaks.module;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.camtweaks.ToggleModuleEvent;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.module.settings.Setting;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// clase base para los módulos
public class Module {

    // atributos
    private String name;
    private String description;
    private boolean enabled;

    // dejar al usuario elegir si el módulo debería apagarse tras soltar su tecla asignada, o si la tecla debería alternar su estado
    protected EnumSetting<KeyModes> keyMode = new EnumSetting<>("modo de tecla", "cómo interpretar la tecla configurada", KeyModes.class, KeyModes.TOGGLE);
    protected KeySetting bind = new KeySetting("tecla", "tecla asignada al módulo, ESC para desactivar", GLFW.GLFW_KEY_UNKNOWN);
    protected BooleanSetting notify = new BooleanSetting("notificar", "notificar en el chat al activar / desactivar", true);

    private final List<Setting> settings = new ArrayList<>();

    protected MinecraftClient mc = MinecraftClient.getInstance();

    public Module(String name, String description) {
        this.name = name;
        this.description = description;
        addSettings(bind, keyMode, notify);
    }


    // ajustes
    public List<Setting> getSettings() {
        return settings;
    }

    @SuppressWarnings("unchecked")
    public <S extends Setting> S addSetting(Setting setting) {
        settings.add(setting);
        setting.setModule(this);
        return (S) setting;  // por convenciencia
    }

    public void addSettings(Setting... settings) {
        for (Setting setting : settings) addSetting(setting);
    }


    // encendido y apagado del módulo
    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
        CameraTweaks.EVENT_BUS.post(new ToggleModuleEvent(this));
    }

    public void onEnable() {
        if (notify.isEnabled()) ChatUtil.info(getName() + " fue activado");
        for (Method method : getClass().getDeclaredMethods())
            if (method.isAnnotationPresent(SubscribeEvent.class)) {
                CameraTweaks.EVENT_BUS.register(this);
                break;
        }
        CameraTweaks.EVENT_BUS.post(new ToggleModuleEvent(this));
    }

    public void onDisable() {
        if (notify.isEnabled()) ChatUtil.info(getName() + " fue desactivado");
        if (CameraTweaks.EVENT_BUS.isRegistered(this))
            CameraTweaks.EVENT_BUS.unregister(this);
        CameraTweaks.EVENT_BUS.post(new ToggleModuleEvent(this));
    }


    // otros métodos
    public void onTick() {}
    public void onKey(int key, int action) {}

    // getters y setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;  // para evitar NPEs al cargar ajustes porque soy imbécil
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public boolean shouldToggleOnBindRelease() {
        return keyMode.is(KeyModes.HOLD);
    }

    public int getKey() {
        return bind.getKey();
    }

    public void setKey(int key) {
        bind.setKey(key);
    }

    public void assignKey(int key) {
        bind.setDefaultKey(key);
        bind.setKey(key);
    }

    public KeySetting getBind() {
        return bind;
    }

    public enum KeyModes {
        HOLD("mantener"),
        TOGGLE("alternar");

        // ojalá hubiera una manera de evitar repetirse con esto pero no la hay (creo) ...
        private final String name;
        KeyModes(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}