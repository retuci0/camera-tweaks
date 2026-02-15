package me.retucio.sputnik.module.modules.client;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.event.TickEvent;
import me.retucio.sputnik.event.sputnik.LoadClickGUIEvent;
import me.retucio.sputnik.event.sputnik.LoadCommandManagerEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ChatPlus;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.module.setting.settings.StringSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.Colors;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

// módulo no visible solo para los ajustes de la interfaz (y del cliente en general)
public class GUI extends Module {

    private final SettingGroup sgWidgets = addSg(new SettingGroup("widgets", true));
    private final SettingGroup sgUi = addSg(new SettingGroup("interfaz", true));
    private final SettingGroup sgChat = addSg(new SettingGroup("chat", true));
    private final SettingGroup sgMisc = addSg(new SettingGroup("misc.", true));

    public final ColorSetting color = sgGeneral.add(new ColorSetting(
            "color",
            "color principal de la interfaz y el mod",
            new Color(70, 20, 210, 230),
            false
    ));

    // números negativos para deslizamiento inverso, 0 para desactivar
    public final NumberSetting scrollSens = sgWidgets.add(new NumberSetting(
            "sensibilidad del scroll",
            "qué tan sensible es la interfaz a la rueda del ratón",
            5,
            -15,
            15,
            0.5
    ));

    public final BooleanSetting scrollBar = sgWidgets.add(new BooleanSetting(
            "barra de desplazamiento",
            "renderizar una barra de desplazamiento a la derecha de la interfaz",
            true
    ));

    public final BooleanSetting searchBar = sgWidgets.add(new BooleanSetting(
            "barra de búsqueda",
            "renderizar una barra de búsqueda que filtra resultados en todos los marcos abiertos",
            true
    ));

    public final BooleanSetting matchCase = sgWidgets.add(new BooleanSetting(
            "distinguir mayúsculas",
            "la búsqueda es sensible a mayúsculas y minúsculas",
            false
    ));
    // ^^^ no sé de qué sirve porque está todo en minúsculas pero bueno

    public final BooleanSetting blur = sgUi.add(new BooleanSetting(
            "desenfoque",
            "desenfocar el fondo mientras la interfaz está abierta",
            true
    ));

    public final StringSetting watermark = sgUi.add(new StringSetting(
            "marca de agua",
            "marca de agua para interfaces (dejar vacío para desactivar)",
            Sputnik.getVersionName(),
            40
    ));

    public final StringSetting commandPrefix = sgChat.add(new StringSetting(
            "prefijo",
            "prefijo de los comandos",
            "$",
            10
    ));

    public final StringSetting chatName = sgChat.add(new StringSetting(
            "nombre",
            "qué nombre usar en notificaciones por el chat",
            "smegma",
            20
    ));

    public final BooleanSetting multipleKeybinds = sgMisc.add(new BooleanSetting(
            "teclas multimódulo",
            "permitir asignar la misma tecla a más de un módulo o acción",
            false
    ));

    public final StringSetting windowTitle = sgMisc.add(new StringSetting(
            "título",
            "título de la ventana",
            Sputnik.getVersionName(),
            50
    ));

    public GUI() {
        super("interfaz",
                "ajustes de la interfaz, y otros misceláneos",
                Category.CLIENT,
                GLFW.GLFW_KEY_RIGHT_SHIFT);

        Sputnik.EVENT_BUS.subscribe(this);
        keyMode.setVisible(false);
        notify.setVisible(false);
        searchBar.onUpdate(v -> matchCase.setVisible(v));
    }

    @EventListener
    public void onLoadCommandManager(LoadCommandManagerEvent event) {
        commandPrefix.onUpdate(CommandManager.INSTANCE::setPrefix);
        commandPrefix.setDefaultValue(CommandManager.INSTANCE.getPrefix());

        chatName.onUpdate(name -> {
            ChatUtil.updatePrefix(name);
            ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class).updateClientName();
        });
    }

    @EventListener
    public void onLoadClickGUI(LoadClickGUIEvent event) {
        color.onUpdate(v -> {
            Colors.red = color.getR();
            Colors.green = color.getG();
            Colors.blue = color.getB();
            Colors.alpha = color.getA();
            Colors.updateAllColors(new Color(Colors.red, Colors.green, Colors.blue, Colors.alpha));
        });
    }

    @EventListener
    public void onTick(TickEvent.Post event) {
        if (color.isRainbow()) {
            Colors.red = color.getR();
            Colors.green = color.getG();
            Colors.blue = color.getB();
            Colors.updateAllColors(new Color(Colors.red, Colors.green, Colors.blue, Colors.alpha));
        }
    }
}
