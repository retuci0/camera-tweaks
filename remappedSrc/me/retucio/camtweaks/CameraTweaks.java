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

import me.retucio.camtweaks.ui.ClickGUI;
import me.retucio.camtweaks.ui.buttons.BindButton;
import me.retucio.camtweaks.ui.buttons.SettingButton;
import me.retucio.camtweaks.ui.buttons.TextButton;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.ui.frames.SettingsFrame;

import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.KeyUtil;
import me.retucio.camtweaks.util.Lists;
import me.retucio.camtweaks.util.MiscUtil;


import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import net.minecraft.client.gui.screen.TitleScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lwjgl.glfw.GLFW;



public class CameraTweaks implements ClientModInitializer {

    // cosas necesarias
    public static final String MOD_ID = "camtweaks";
    public static final CameraTweaks INSTANCE = new CameraTweaks();
    public static final Logger LOGGER = LogManager.getLogger(CameraTweaks.class);
    public static final EventBus EVENT_BUS = new EventBus();
    public static MinecraftClient mc;

    private Screen prevScreen;
    public boolean settingsApplied = false;

    @Override
    public void onInitializeClient() {
        // por algún motivo aunque lo añada a los args de la JVM sigue siendo true a menos que haga esto??? puta vida
        System.setProperty("java.awt.headless", "false");

        mc = MinecraftClient.getInstance();

        EVENT_BUS.register(this);
        EVENT_BUS.register(MiscUtil.class);
        EVENT_BUS.register(ChatUtil.class);

        Lists.init();

        ModuleManager.INSTANCE = new ModuleManager(EVENT_BUS);
        EVENT_BUS.post(new LoadModuleManagerEvent());

        CommandManager.INSTANCE = new CommandManager();
        EVENT_BUS.post(new LoadCommandManagerEvent());

        ClickGUI.INSTANCE = new ClickGUI();
        EVENT_BUS.post(new LoadClickGUIEvent());

        ConfigManager.load();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConfigManager.save();
            EVENT_BUS.post(new ShutdownEvent());
        }));
    }

    // se ejecuta cada tick, es decir, 20 veces por segundo
    public void onTick() {
        ModuleManager.INSTANCE.getEnabledModules().forEach(Module::onTick);

        if (!settingsApplied
                && ConfigManager.getConfig() != null
                && mc.player != null && mc.world != null) {
            ConfigManager.applyConfig();
            settingsApplied = true;
        }
    }


    // se ejecuta cada vez que se presiona una tecla
    public void onKeyPress(int key, int action) {
        ModuleManager.INSTANCE.getEnabledModules().forEach(module -> module.onKey(key, action));

        boolean anyFocused = isAnySettingButtonFocused() || ClickGUI.INSTANCE.getSearchBar().isFocused();
        ClickGUI.INSTANCE.setAnyFocused(anyFocused);

        if (action == GLFW.GLFW_PRESS) {
            if (BindCommand.onKeyPress(key)) return;
            handleSettingButtonsKey(key, action);
            handleModuleToggle(key, anyFocused);
            handleClickGUIKey(key, anyFocused);
        } else if (action == GLFW.GLFW_RELEASE && !anyFocused) {
            handleModuleRelease(key);
        }
    }

    // verifica si algún botón de ajustes está escuchando
    private boolean isAnySettingButtonFocused() {
        for (SettingsFrame sf : ClickGUI.INSTANCE.getSettingsFrames())
            for (SettingButton sb : sf.getButtons())
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
                if (sb instanceof BindButton b) b.onKey(key, action);
                else if (sb instanceof TextButton t) t.onKey(key, action);
            }
        }
    }

    // maneja la lógica de apertura de la interfaz
    private void handleClickGUIKey(int key, boolean anyFocused) {
        if (key != ClientSettingsFrame.guiSettings.getKey() || anyFocused) return;

        // al parecer esto hace que con la tecla de la interfaz puedas ir cambiando de splash text en la pantalla del título
        // pero me ha hecho gracia así que así se queda
        if (mc.currentScreen != ClickGUI.INSTANCE && !(mc.currentScreen instanceof TitleScreen)) {
            prevScreen = mc.currentScreen;
            mc.setScreen(ClickGUI.INSTANCE);
        } else {
            ClickGUI.INSTANCE.close();
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

    @SubscribeEvent
    public void onStop(ShutdownEvent event) {
        ConfigManager.save();
    }
}