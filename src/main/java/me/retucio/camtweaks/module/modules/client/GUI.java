package me.retucio.camtweaks.module.modules.client;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadClickGUIEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadCommandManagerEvent;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.misc.ChatPlus;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.module.settings.StringSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Colors;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

// módulo no visible solo para los ajustes de la interfaz (y del cliente en general)
public class GUI extends Module {

    public ColorSetting color = addSetting(new ColorSetting("color", "color principal de la interfaz y el mod",
            new Color(70, 20, 210, 230), false));

    // números negativos para deslizamiento inverso, 0 para desactivar
    public NumberSetting scrollSens = addSetting(new NumberSetting("sensibilidad del scroll", "qué tan sensible es la interfaz a la rueda del ratón",
            5, -15, 15, 0.5));
    public BooleanSetting scrollBar = addSetting(new BooleanSetting("barra de desplazamiento", "renderizar una barra de desplazamiento a la derecha de la interfaz", true));

    public BooleanSetting searchBar = addSetting(new BooleanSetting("barra de búsqueda", "renderizar una barra de búsqueda que filtra resultados en todos los marcos abiertos", true));
    public BooleanSetting matchCase = addSetting(new BooleanSetting("distinguir mayúsculas", "la búsqueda es sensible a mayúsculas y minúsculas", false));
    // ^^^ no sé de qué sirve porque está todo en minúsculas pero bueno

    public BooleanSetting blur = addSetting(new BooleanSetting("desenfoque", "desenfocar el fondo mientras la interfaz está abierta", true));
    public StringSetting watermark = addSetting(new StringSetting("marca de agua", "marca de agua para interfaces (dejar vacío para desactivar)", CameraTweaks.getVersionName(), 40));

    public StringSetting commandPrefix = addSetting(new StringSetting("prefijo", "prefijo de los comandos", "$", 10));
    public StringSetting chatName = addSetting(new StringSetting("nombre", "qué nombre usar en notificaciones por el chat", "smegma", 20));

    public BooleanSetting multipleKeybinds = addSetting(new BooleanSetting("teclas multimódulo", "permitir asignar la misma tecla a más de un módulo", false));

    public GUI() {
        super("interfaz",
                "ajustes de la interfaz, y otros misceláneos",
                Category.CLIENT,
                GLFW.GLFW_KEY_RIGHT_SHIFT);

        CameraTweaks.EVENT_BUS.register(this);
        keyMode.setVisible(false);
        notify.setVisible(false);
        searchBar.onUpdate(v -> matchCase.setVisible(v));
    }

    @SubscribeEvent
    public void onLoadCommandManager(LoadCommandManagerEvent event) {
        commandPrefix.onUpdate(CommandManager.INSTANCE::setPrefix);
        commandPrefix.setDefaultValue(CommandManager.INSTANCE.getPrefix());

        chatName.onUpdate(name -> {
            ChatUtil.updatePrefix(name);
            ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class).updateClientName();
        });
    }

    @SubscribeEvent
    public void onLoadClickGUI(LoadClickGUIEvent event) {
        color.onUpdate(v -> {
            Colors.red = color.getR();
            Colors.green = color.getG();
            Colors.blue = color.getB();
            Colors.alpha = color.getA();
            Colors.updateAllColors(new Color(Colors.red, Colors.green, Colors.blue, Colors.alpha));
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.Post event) {
        if (color.isRainbow()) {
            Colors.red = color.getR();
            Colors.green = color.getG();
            Colors.blue = color.getB();
            Colors.updateAllColors(new Color(Colors.red, Colors.green, Colors.blue, Colors.alpha));
        }
    }
}
