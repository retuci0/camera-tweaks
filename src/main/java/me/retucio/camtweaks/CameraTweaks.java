package me.retucio.camtweaks;


import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.command.commands.BindCommand;

import me.retucio.camtweaks.config.ConfigManager;

import me.retucio.camtweaks.event.EventBus;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.ShutdownEvent;

import me.retucio.camtweaks.event.events.camtweaks.LoadClickGUIEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadCommandManagerEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadModuleManagerEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;

import me.retucio.camtweaks.module.modules.HUD;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.hud.HudEditorScreen;
import me.retucio.camtweaks.ui.hud.HudRenderer;
import me.retucio.camtweaks.ui.widgets.buttons.settings.BindButton;
import me.retucio.camtweaks.ui.widgets.buttons.SettingButton;
import me.retucio.camtweaks.ui.widgets.buttons.settings.TextButton;
import me.retucio.camtweaks.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;

import me.retucio.camtweaks.ui.widgets.Button;
import me.retucio.camtweaks.util.*;


import me.retucio.camtweaks.util.render.DrawUtil;
import me.retucio.camtweaks.util.render.RenderUtil;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;

import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lwjgl.glfw.GLFW;



public class CameraTweaks implements ClientModInitializer {

    // cosas necesarias
    public static final CameraTweaks INSTANCE = new CameraTweaks();
    public static final EventBus EVENT_BUS = new EventBus();
    public static final Logger LOGGER = LogManager.getLogger(CameraTweaks.class);
    public static MinecraftClient mc;

    // id y versión
    public static final String MOD_ID = "camtweaks";
    public static final String MOD_VERSION;

    static {
        MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }


    private Screen prevScreen;
    public static boolean settingsApplied = false;


    @Override
    public void onInitializeClient() {
        // por algún motivo aunque lo añada a los args de la JVM sigue siendo true a menos que haga esto??? puta vida
        System.setProperty("java.awt.headless", "false");

        mc = MinecraftClient.getInstance();
        ConfigManager.load();

        EVENT_BUS.register(this);

        EVENT_BUS.register(HudRenderer.class);

        EVENT_BUS.register(ChatUtil.class);
        EVENT_BUS.register(DrawUtil.class);
        EVENT_BUS.register(EntityUtil.class);
        EVENT_BUS.register(InventoryUtil.class);
        EVENT_BUS.register(MiscUtil.class);
        EVENT_BUS.register(NetworkUtil.class);
        EVENT_BUS.register(RenderUtil.class);

        Lists.init();

        ModuleManager.INSTANCE = new ModuleManager();
        EVENT_BUS.post(new LoadModuleManagerEvent());

        CommandManager.INSTANCE = new CommandManager();
        EVENT_BUS.post(new LoadCommandManagerEvent());

        ClickGUI.INSTANCE = new ClickGUI();
        HudEditorScreen.INSTANCE = new HudEditorScreen();
        EVENT_BUS.post(new LoadClickGUIEvent());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConfigManager.save();
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
    }


    // se ejecuta cada vez que se presiona una tecla
    public void onKeyPress(int key, int action) {
        if (key == GLFW.GLFW_KEY_F3) return;  // prevenir activación de módulos al usar combinaciones de F3
        ModuleManager.INSTANCE.getEnabledModules().forEach(module -> module.onKey(key, action));

        boolean anyFocused = isAnySettingButtonFocused() || ClickGUI.INSTANCE.getSearchBar().isFocused();
        ClickGUI.INSTANCE.setAnyFocused(anyFocused);

        if (action != GLFW.GLFW_RELEASE) {
            if (BindCommand.onKeyPress(key)) return;
            if (action == GLFW.GLFW_PRESS) {
                handleModuleToggle(key, anyFocused);
                handleClickGUIKey(key, anyFocused);
                handleHudEditorKey(key, anyFocused);
            }
            handleSettingButtonsKey(key, action);
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
        if (mc.currentScreen != null && mc.currentScreen != ClickGUI.INSTANCE) return;

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
            for (SettingButton sb : sf.getButtons()) {
                if (sb instanceof BindButton bb) bb.onKey(key, action);
                if (sb instanceof TextButton tb) tb.onKey(key, action);
            }
        }
    }

    // maneja la lógica de apertura de la interfaz
    private void handleClickGUIKey(int key, boolean anyFocused) {
        if (key != ClientSettingsFrame.guiSettings.getKey() || anyFocused || isOnTypingScreen())
            return;

        // al parecer esto hace que con la tecla de la interfaz puedas ir cambiando de splash text en la pantalla del título
        // pero me ha hecho gracia así que así se queda
        if (mc.currentScreen != ClickGUI.INSTANCE) {
            prevScreen = mc.currentScreen;
            mc.setScreen(ClickGUI.INSTANCE);
        } else {
            ClickGUI.INSTANCE.close();
            mc.setScreen(prevScreen);
        }
    }

    // manejar la lógica de apertura del editor de elementos del hud, con la misma lógica que handleClickGUIKey()
    private void handleHudEditorKey(int key, boolean anyFocused) {
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (key != hud.editorKey.getKey() || anyFocused || isOnTypingScreen()) return;

        if (mc.currentScreen != HudEditorScreen.INSTANCE) {
            if (!hud.isEnabled()) {
                ChatUtil.warn("HUD está desactivado, lumbreras");
                return;
            }
            prevScreen = mc.currentScreen;
            mc.setScreen(HudEditorScreen.INSTANCE);
        } else {
            HudEditorScreen.INSTANCE.close();
            mc.setScreen(prevScreen);
        }
    }

    // se ocupa de apagar los módulos que tengan configurado hacerlo tras soltar su tecla
    private void handleModuleRelease(int key) {
        if (mc.currentScreen != null && mc.currentScreen != ClickGUI.INSTANCE) return;

        for (Module module : ModuleManager.INSTANCE.getEnabledModules())
            if (module.shouldToggleOnBindRelease() && key == module.getKey())
                module.setEnabled(false);
    }

    private boolean isOnTypingScreen() {
                return mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof AnvilScreen
                || mc.currentScreen instanceof AbstractSignEditScreen
                || mc.currentScreen instanceof AbstractCommandBlockScreen;
    }

    @SubscribeEvent
    public void onStop(ShutdownEvent event) {
        ConfigManager.save();
    }

    public static String getVersionName() {
        return MOD_ID + "_v" + MOD_VERSION + "_" + SharedConstants.getGameVersion().name();
    }
}