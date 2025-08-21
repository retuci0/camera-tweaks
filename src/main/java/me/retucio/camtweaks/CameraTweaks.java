package me.retucio.camtweaks;

import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.command.commands.BindCommand;
import me.retucio.camtweaks.event.EventBus;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.ui.ClickGUI;
import me.retucio.camtweaks.ui.settings.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class CameraTweaks implements ModInitializer {

    // cosas necesarias
    public static final String MOD_ID = "camtweaks";
    public static final CameraTweaks INSTANCE = new CameraTweaks();
    public static final Logger LOGGER = LogManager.getLogger(CameraTweaks.class);
    public static final EventBus EVENT_BUS = new EventBus();
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    private Screen prevScreen;

    // managers
    public static ModuleManager moduleManager = new ModuleManager(EVENT_BUS);
    public static CommandManager commandManager = new CommandManager();

    @Override
    public void onInitialize() {}

    // se ejecuta cada vez que se presiona una tecla
    public void onKeyPress(int key, int action) {
        moduleManager.getEnabledModules().forEach(module -> module.onKey(key, action));

        boolean anyFocused = isAnySettingButtonFocused();

        if (action == GLFW.GLFW_PRESS) {
            handleModuleToggle(key, anyFocused);
            handleSettingButtonsKey(key, action);
            handleClickGUIKey(key, anyFocused);
            BindCommand.onKeyPress(key);
        } else if (action == GLFW.GLFW_RELEASE && !anyFocused) {
            handleModuleRelease(key);
        }
    }

    // verifica si algún botón de ajustes está escuchando
    private boolean isAnySettingButtonFocused() {
        for (SettingsFrame sf : ClickGUI.INSTANCE.getSettingsFrames()) {
            for (SettingButton sb : sf.getButtons()) {
                if ((sb instanceof BindButton b && b.isFocused()) || (sb instanceof TextButton t && t.isFocused()))
                    return true;
            }
        }
        return false;
    }

    // se ocupa de la lógica de encendido y apagado de los módulos
    private void handleModuleToggle(int key, boolean anyFocused) {
        if (mc.currentScreen != null && mc.currentScreen != ClickGUI.INSTANCE) return;

        for (Module module : moduleManager.getModules()) {
            if (key != module.getKey() || anyFocused) continue;

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
        if (key != ClickGUISettingsFrame.guiSettings.getKey() || anyFocused) return;

        if (mc.currentScreen != ClickGUI.INSTANCE) {
            prevScreen = mc.currentScreen;
            mc.setScreen(ClickGUI.INSTANCE);
        } else {
            mc.setScreen(prevScreen);
        }
    }

    // se ocupa de apagar los módulos que tengan configurado hacerlo tras soltar su tecla
    private void handleModuleRelease(int key) {
        if (mc.currentScreen != null && mc.currentScreen != ClickGUI.INSTANCE) return;

        for (Module module : moduleManager.getEnabledModules()) {
            if (module.shouldToggleOnBindRelease() && key == module.getKey()) {
                module.setEnabled(false);
            }
        }
    }

    // se ejecuta cada tick, es decir, 20 veces por segundo
    public void onTick() {
        if (mc.player == null) return;
        moduleManager.getEnabledModules().forEach(Module::onTick);
    }
}