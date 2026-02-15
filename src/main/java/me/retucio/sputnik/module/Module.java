package me.retucio.sputnik.module;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.event.sputnik.ToggleModuleEvent;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.module.setting.Setting;
import me.retucio.sputnik.util.ChatUtil;
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
    private Category category;

    private boolean enabled;
    private boolean saveSettings = true;
    private boolean searchMatch = true;

    // dejar al usuario elegir si el módulo debería apagarse tras soltar su tecla asignada, o si la tecla debería alternar su estado
    protected EnumSetting<KeyModes> keyMode = new EnumSetting<>("modo de tecla", "cómo interpretar la tecla configurada", KeyModes.class, KeyModes.TOGGLE);
    protected KeySetting bind = new KeySetting("tecla", "tecla asignada al módulo, ESC para desactivar", GLFW.GLFW_KEY_UNKNOWN);
    protected BooleanSetting notify = new BooleanSetting("notificar", "notificar en el chat al activar / desactivar", true);

    protected final List<SettingGroup> sgs = new ArrayList<>();
    protected final SettingGroup sgGeneral = addSg(new SettingGroup("general", true));

    protected MinecraftClient mc = MinecraftClient.getInstance();


    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category =  category;
        sgGeneral.addAll(bind, keyMode, notify);
    }

    public Module(String name, String description, Category category, int key) {
        this.name = name;
        this.description = description;
        this.category = category;
        sgGeneral.addAll(bind, keyMode, notify);
        this.bind.setDefaultValue(key);

        // puto marcos
        if (ConfigManager.getConfig().settings
                .get(this.getName() + ":" + bind.getName()) == null)
            this.bind.setValue(key);
    }


    // ajustes

    public SettingGroup addSg(SettingGroup sg) {
        sgs.add(sg);
        sg.setModule(this);
        return sg;
    }

    public List<SettingGroup> getSgs() {
        return sgs;
    }

    public SettingGroup getSg(String name) {
        for (SettingGroup sg : sgs) {
            if (sg.getName().equals(name)) {
                return sg;
            }
        }
        return null;
    }

    public SettingGroup getSgGeneral() {
        return sgGeneral;
    }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> settings = new ArrayList<>();
        sgs.forEach(sg -> sg.forEach(settings::add));
        return settings;
    }


    // encendido y apagado del módulo
    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
        Sputnik.EVENT_BUS.post(new ToggleModuleEvent(this));
    }

    public void onEnable() {
        if (notify.getValue()) ChatUtil.info(getName() + " fue activado");
        for (Method method : getClass().getDeclaredMethods())
            if (method.isAnnotationPresent(EventListener.class)) {
                Sputnik.EVENT_BUS.subscribe(this);
                break;
        }
        Sputnik.EVENT_BUS.post(new ToggleModuleEvent(this));
    }

    public void onDisable() {
        if (notify.getValue()) ChatUtil.info(getName() + " fue desactivado");
        if (Sputnik.EVENT_BUS.isSubscribed(this))
            Sputnik.EVENT_BUS.unsubscribe(this);
        Sputnik.EVENT_BUS.post(new ToggleModuleEvent(this));
    }


    // tick (ejecutado 20 veces por segundo)
    public void onTick() {}

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public boolean shouldSaveSettings() {
        return saveSettings;
    }

    public void shouldSaveSettings(boolean value) {
        this.saveSettings = value;
    }

    public int getKey() {
        return bind.getValue();
    }

    public void setKey(int key) {
        bind.setValue(key);
    }

    public void assignKey(int key) {
        bind.setDefaultValue(key);
        bind.setValue(key);
    }

    public KeySetting getBind() {
        return bind;
    }

    public boolean isSearchMatch() {
        return searchMatch;
    }

    public void setSearchMatch(boolean searchMatch) {
        this.searchMatch = searchMatch;
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