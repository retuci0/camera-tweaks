package me.retucio.camtweaks.module;

import me.retucio.camtweaks.event.EventBus;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.module.modules.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// donde se registran los módulos, y los "listeners" de eventos en cada módulo que lo necesite
public class ModuleManager {

    public static ModuleManager INSTANCE;

    private final List<Module> modules = new ArrayList<>();
    private final EventBus eventBus;

    public ModuleManager(EventBus bus) {
        this.eventBus = bus;
        addModules();
    }

    // por orden alfabético y todo, flipas
    private void addModules() {
        modules.add(new AntiInvis());
        modules.add(new BlockOutline());
        modules.add(new BossbarStack());
        modules.add(new ChatPlus());
        modules.add(new CritsPlus());
        modules.add(new DamageOverlay());
        modules.add(new FakePlayer());
        modules.add(new Freecam());
        modules.add(new Freelook());
        modules.add(new Fullbright());
        modules.add(new HandView());
        modules.add(new HUD());
        modules.add(new LogoutSpots());
        modules.add(new Nametags());
        modules.add(new NoRender());
        modules.add(new PerspectivePlus());
        modules.add(new PortalGUI());
        modules.add(new ScreenshotPlus());
        modules.add(new ShulkerPeek());
        modules.add(new TimeChanger());
//        modules.add(new TooltipsPlus());
        modules.add(new Zoom());

        modules.sort(Comparator.comparing(module -> module.getName().toLowerCase()));

        // registrar los "listeners" necesarios
        for (Module module : getEnabledModules()) {
            for (Method method : module.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(SubscribeEvent.class)) {
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