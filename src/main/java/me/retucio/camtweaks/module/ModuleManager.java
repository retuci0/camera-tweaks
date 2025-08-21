package me.retucio.camtweaks.module;

import me.retucio.camtweaks.event.EventBus;
import me.retucio.camtweaks.event.Subscribe;
import me.retucio.camtweaks.module.modules.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


// donde se regisran los módulos, y los "listeners" de eventos en cada módulo que lo necesite
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final EventBus eventBus;

    public ModuleManager(EventBus bus) {
        this.eventBus = bus;
        addModules();
    }

    private void addModules() {
        modules.add(new Fullbright());
        modules.add(new Zoom());
        modules.add(new Perspective());
        modules.add(new HandView());
        modules.add(new BetterChat());

        // registrar los "listeners" necesarios
        for (Module module : getEnabledModules()) {
            for (Method method : module.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Subscribe.class)) {
                    eventBus.register(module);
                    break;
                }
            }
        }
    }


    // para obtener módulos más fácilmente (por nombre, clase, o la lista completa)
    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getEnabledModules() {
        List<Module> enabledModules = new ArrayList<>();
        for (Module module : modules)
            if (module.isEnabled()) enabledModules.add(module);

        return enabledModules;
    }

    public Module getModuleByName(String name) {
        for (Module module : modules)
            if (module.getName().equalsIgnoreCase(name)) return module;
        return null;
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for (Module module : modules)
            if (clazz.isInstance(module))
                return clazz.cast(module);
        return null;
    }
}