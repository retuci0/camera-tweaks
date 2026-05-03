package me.retucio.sputnik;

import com.github.retucio.neutrino.EventBus;
import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.cape.CapeManager;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.command.commands.BindCommand;

import me.retucio.sputnik.config.ConfigManager;

import me.retucio.sputnik.event.ShutdownEvent;
import me.retucio.sputnik.event.interact.OpenScreenEvent;
import me.retucio.sputnik.event.sputnik.LoadCapeManagerEvent;
import me.retucio.sputnik.event.sputnik.LoadClickGUIEvent;
import me.retucio.sputnik.event.sputnik.LoadCommandManagerEvent;
import me.retucio.sputnik.event.sputnik.LoadModuleManagerEvent;

import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;

import me.retucio.sputnik.ui.hud.HudRenderer;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.hud.HudEditorScreen;
import me.retucio.sputnik.ui.screen.UpdateScreen;
import me.retucio.sputnik.ui.widgets.buttons.settings.BindButton;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.widgets.buttons.settings.TextButton;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import me.retucio.sputnik.ui.widgets.Button;

import me.retucio.sputnik.util.*;
import me.retucio.sputnik.util.KeyUtil;
import me.retucio.sputnik.util.misc.BlockIterator;
import me.retucio.sputnik.util.misc.VersionChecker;
import me.retucio.sputnik.util.render.DrawUtil;
import me.retucio.sputnik.util.render.RenderUtil;

import me.retucio.sputnik.util.render.Textures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lwjgl.glfw.GLFW;


public class Sputnik implements ClientModInitializer {

    // cosas necesarias
    public static final Sputnik INSTANCE = new Sputnik();
    public static final EventBus EVENT_BUS = new EventBus();
    public static final Logger LOGGER = LogManager.getLogger(Sputnik.class);
    public static final long LAUNCH_TIME = System.currentTimeMillis();
    public static Minecraft mc;

    // id y versión
    public static final String MOD_ID = "sputnik";
    public static final String MOD_VERSION = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();

    private Screen prevScreen;
    public static boolean settingsApplied = false;

    static {
        // puta vida
        System.setProperty("java.awt.headless", "false");
    }

    @Override
    public void onInitializeClient() {
        mc = Minecraft.getInstance();
        ConfigManager.load();

        VersionChecker.check();

        EVENT_BUS.subscribe(this);

        EVENT_BUS.subscribe(ChatUtil.class);
        EVENT_BUS.subscribe(DrawUtil.class);
        EVENT_BUS.subscribe(EntityUtil.class);
        EVENT_BUS.subscribe(InventoryUtil.class);
        EVENT_BUS.subscribe(MiscUtil.class);
        EVENT_BUS.subscribe(NetworkUtil.class);
        EVENT_BUS.subscribe(RenderUtil.class);

        EVENT_BUS.subscribe(BlockIterator.class);

        Lists.init();

        CapeManager.INSTANCE = new CapeManager();
        EVENT_BUS.post(new LoadCapeManagerEvent());

        ModuleManager.INSTANCE = new ModuleManager();
        EVENT_BUS.post(new LoadModuleManagerEvent());

        CommandManager.INSTANCE = new CommandManager();
        EVENT_BUS.post(new LoadCommandManagerEvent());

        mc.execute(() -> {
            Textures.init();
            ClickGUI.INSTANCE = new ClickGUI();
            HudEditorScreen.INSTANCE = new HudEditorScreen();
            EVENT_BUS.post(new LoadClickGUIEvent());
            HudRenderer.init();
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EVENT_BUS.post(new ShutdownEvent());
        }));
    }

    // se ejecuta cada tick, es decir, 20 veces por segundo
    public void onTick() {
        if (!settingsApplied
                && ConfigManager.getConfig() != null
                && !ConfigManager.hasLoaded()) {
            ConfigManager.applyConfig();
            settingsApplied = true;
        }

        ModuleManager.INSTANCE.getEnabledModules().forEach(Module::onTick);
        mc.getWindow().setTitle(ClientSettingsFrame.guiSettings.windowTitle.getValue());
    }


    // se ejecuta cada vez que se presiona una tecla
    public void onInput(int key, int action) {
        // prevenir interrumpir combos de F3
        if (key == GLFW.GLFW_KEY_F3) return;

        boolean anyFocused = isAnySettingButtonFocused() || ClickGUI.INSTANCE.getSearchBar().isFocused();
        ClickGUI.INSTANCE.setAnyFocused(anyFocused);

        if (action != GLFW.GLFW_RELEASE) {
            if (BindCommand.onKeyPress(key)) return;
            if (action == GLFW.GLFW_PRESS) {
                handleModuleToggle(key, anyFocused);
                handleClickGUIKey(key, anyFocused);
                handleHudEditorKey(key, anyFocused);
            }
            // solo reenviar teclas del teclado
            if (key >= 32) {
                handleSettingButtonsKey(key, action);
            }
        } else if (!anyFocused) {
            handleModuleRelease(key);
        }
    }

    // verifica si algún botón de ajustes está escuchando
    private boolean isAnySettingButtonFocused() {
        for (SettingsFrame sf : ClickGUI.INSTANCE.getSettingsFrames())
            for (Button sb : sf.getButtons())
                if ((sb instanceof BindButton b && b.isFocused()) || (sb instanceof TextButton t && t.isFocused()))
                    return true;
        return false;
    }

    // se ocupa de la lógica de encendido y apagado de los módulos
    private void handleModuleToggle(int key, boolean anyFocused) {
        if (mc.screen != null && mc.screen != ClickGUI.INSTANCE) return;

        for (Module module : ModuleManager.INSTANCE.getModules()) {
            if (key != module.getKey() || anyFocused || KeyUtil.isKeyDown(GLFW.GLFW_KEY_F3)) continue;  // evitar interrumpir combinaciones de teclas del F3

            if (module.shouldToggleOnBindRelease() && !module.isEnabled())
                module.setEnabled(true);
            else if (!module.shouldToggleOnBindRelease())
                module.toggle();
        }
    }

    // se ocupa de hacer los botones de ajustes que lo necesiten escuchar teclas
    private void handleSettingButtonsKey(int key, int action) {
        for (SettingsFrame sf : ClickGUI.INSTANCE.getSettingsFrames()) {
            for (SettingButton<?> sb : sf.getButtons()) {
                if (sb instanceof BindButton bb) bb.onKey(key, action);
                if (sb instanceof TextButton tb) tb.onKey(key, action);
            }
        }
    }

    // maneja la lógica de apertura de la interfaz
    private void handleClickGUIKey(int key, boolean anyFocused) {
        if (key != ClientSettingsFrame.guiSettings.getKey() || anyFocused || isOnTypingScreen())
            return;

        if (mc.screen != ClickGUI.INSTANCE) {
            prevScreen = mc.screen;
            mc.setScreen(ClickGUI.INSTANCE);
        } else {
            ClickGUI.INSTANCE.onClose();
            mc.setScreen(prevScreen);
        }
    }

    // manejar la lógica de apertura del editor de elementos del hud, con la misma lógica que handleClickGUIKey()
    private void handleHudEditorKey(int key, boolean anyFocused) {
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (key != hud.editorKey.getValue() || anyFocused || isOnTypingScreen()) return;

        if (mc.screen != HudEditorScreen.INSTANCE) {
            if (!hud.isEnabled()) {
                ChatUtil.warn("HUD está desactivado, lumbreras");
                return;
            }
            prevScreen = mc.screen;
            mc.setScreen(HudEditorScreen.INSTANCE);
        } else {
            HudEditorScreen.INSTANCE.onClose();
            mc.setScreen(prevScreen);
        }
    }

    // se ocupa de apagar los módulos que tengan configurado hacerlo tras soltar su tecla
    private void handleModuleRelease(int key) {
        if (mc.screen != null && mc.screen != ClickGUI.INSTANCE) return;

        for (Module module : ModuleManager.INSTANCE.getEnabledModules())
            if (module.shouldToggleOnBindRelease() && key == module.getKey())
                module.setEnabled(false);
    }

    private boolean isOnTypingScreen() {
                return mc.screen instanceof ChatScreen
                || mc.screen instanceof AnvilScreen
                || mc.screen instanceof AbstractSignEditScreen
                || mc.screen instanceof AbstractCommandBlockEditScreen;
    }

    @EventListener
    // notificar al usuario de que hay una actualización disponible, si la hay
    public void onSetScreen(OpenScreenEvent event) {
        if (VersionChecker.shouldShowScreen && event.getScreen() instanceof TitleScreen) {
            event.cancel();
            VersionChecker.shouldShowScreen = false;
            mc.execute(() -> mc.screen = new UpdateScreen());
        }
    }

    @EventListener
    public void onStop(ShutdownEvent event) {
        ConfigManager.save();
    }

    public static String getVersionName() {
        return MOD_ID + "_v" + MOD_VERSION + "_" + SharedConstants.getCurrentVersion().name();
    }
}