package me.retucio.sputnik.module;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.modules.client.DiscordRPC;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.module.modules.camera.*;
import me.retucio.sputnik.module.modules.combat.AttributeSwapper;
import me.retucio.sputnik.module.modules.combat.MaceKill;
import me.retucio.sputnik.module.modules.combat.ProjectileTrajectories;
import me.retucio.sputnik.module.modules.combat.SpearKill;
import me.retucio.sputnik.module.modules.inventory.*;
import me.retucio.sputnik.module.modules.misc.*;
import me.retucio.sputnik.module.modules.movement.*;
import me.retucio.sputnik.module.modules.network.*;
import me.retucio.sputnik.module.modules.player.*;
import me.retucio.sputnik.module.modules.render.*;
import me.retucio.sputnik.module.modules.render.CritsPlus;
import me.retucio.sputnik.module.modules.world.*;
import me.retucio.sputnik.util.MiscUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// donde se registran los módulos, y los "listeners" de eventos en cada módulo que lo necesite
public class ModuleManager {

    public static ModuleManager INSTANCE;

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        addModules();
    }

    private void addModules() {
        // añadir los módulos
        addCamera();
        addClient();
        addInventory();
        addMisc();
        addMovement();
        addNetwork();
        addPlayer();
        addRender();
        addWorld();

        modules.sort(Comparator.comparing(module -> MiscUtil.removeAccentMarks(module.getName().toLowerCase())));

        // registrar los "listeners" necesarios
        for (Module module : getEnabledModules()) {
            for (Method method : module.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(EventListener.class)) {
                    Sputnik.EVENT_BUS.subscribe(module);
                    break;
                }
            }
        }
    }

    private void add(Module module) {
        modules.add(module);
    }




    private void addCamera() {
        add(new Autism());
        add(new Freecam());
        add(new Freelook());
        add(new Fullbright());
        add(new PerspectivePlus());
        add(new Rotations());
        add(new Zoom());
    }

    private void addClient() {
        add(new DiscordRPC());
        add(new HUD());
    }

    private void addMisc() {
        add(new AntiInvis());
        add(new AnvilFont());
        add(new BookBot());
        add(new BossbarStack());
        add(new ChatPlus());
        add(new FakePlayer());
        add(new ScreenshotPlus());
        add(new UnfocusedCpu());
    }

    private void addInventory() {
        add(new BundleDupe());
        add(new CreativeInventoryHotbarKeybinds());
        add(new InventoryPlus());
        add(new Offhand());
        add(new PortalGUI());
        add(new Replenish());
        add(new ShulkerPeek());
        add(new UIMove());
        add(new XCarry());
    }

    private void addMovement() {
        add(new BoatFly());
        add(new Dolphin());
        add(new ElytraBounce());
        add(new ElytraFly());
        add(new Headhitters());
        add(new InfiniteElytra());
        add(new Jesus());
        add(new ReverseStep());
        add(new SafeWalk());
        add(new Slippy());
        add(new Step());
        add(new TridentBoost());
    }

    private void addNetwork() {
        add(new BungeecordSpoofer());
        add(new LogoutSpots());
        add(new PacketDelay());
        add(new PacketMine());
        add(new Reconnect());
        add(new RPackBypass());
    }

    private void addPlayer() {
        add(new AirPlace());
        add(new AntiHunger());
        add(new AttributeSwapper());
        add(new AutoFish());
        add(new Capes());
        add(new FastUse());
        add(new HandView());
        add(new KeyPearl());
        add(new MaceKill());
        add(new NoFall());
        add(new SpearKill());
        add(new WarnLowDurability());
    }

    private void addRender() {
        add(new BlockESP());
        add(new BlockOutline());
        add(new BreakingProgress());
        add(new CritsPlus());
        add(new DamageOverlay());
        add(new EntityESP());
        add(new Fonts());
        add(new GlintPlus());
        add(new Hitboxes());
        add(new Nametags());
        add(new NoRender());
        add(new Particles());
    }

    private void addWorld() {
        add(new AutoSign());
        add(new AutoTool());
        add(new BlockInfo());
        add(new ColoredSigns());
        add(new LightOverlay());
        add(new NoMiningInterruptions());
        add(new ProjectileTrajectories());
        add(new Racist());
        add(new StrongholdTriangulator());
        add(new TimeChanger());
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
            if (MiscUtil.removeAccentMarks(module.getName()).equalsIgnoreCase(name))
                return module;
        return null;
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for (Module module : modules)
            if (clazz.isInstance(module))
                return clazz.cast(module);
        return null;
    }
}