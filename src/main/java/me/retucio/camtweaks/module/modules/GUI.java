package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadClickGUIEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadCommandManagerEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.module.settings.StringSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Colors;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

// módulo no visible solo para los ajustes de la interfaz (y del cliente en general)
public class GUI extends Module {

    public BooleanSetting rainbow = addSetting(new BooleanSetting("gaming", "gaming", false));
    public NumberSetting rainbowSpeed = addSetting(new NumberSetting("velocidad del gaming", "velocidad, del gaming.",
            1000, 0, 10000, 2));
    public NumberSetting saturation = addSetting(new NumberSetting("saturación del gaming", "saturación, del gaming",
            1, 0, 1, 0.01));
    public NumberSetting brightness = addSetting(new NumberSetting("brillo del gaming", "brillo, del gaming",
            0.8, 0, 1, 0.01));

    public NumberSetting red = addSetting(new NumberSetting("rojo", "cantidad de rojo en el RGB del marco", 70, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "cantidad de verde en el RGB del marco", 20, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "cantidad de azul en el RGB del marco", 210, 0, 255, 1));
    public NumberSetting alpha = addSetting(new NumberSetting("alpha", "opacidad del marco", 230, 0, 255, 1));

    // números negativos para deslizamiento inverso, 0 para desactivar
    public NumberSetting scrollSens = addSetting(new NumberSetting("sensibilidad del scroll", "qué tan sensible es la interfaz a la rueda del ratón",
            5, -15, 15, 0.5));
    public BooleanSetting scrollBar = addSetting(new BooleanSetting("barra de desplazamiento", "renderizar una barra de desplazamiento a la derecha de la interfaz", true));

    public BooleanSetting searchBar = addSetting(new BooleanSetting("barra de búsqueda", "renderizar una barra de búsqueda que filtra resultados en todos los marcos abiertos", true));
    public BooleanSetting matchCase = addSetting(new BooleanSetting("distinguir mayúsculas", "la búsqueda es sensible a mayúsculas y minúsculas", false));
    // ^^^ no sé de qué sirve porque está todo en minúsculas pero bueno

    public BooleanSetting blur = addSetting(new BooleanSetting("desenfoque", "desenfocar el fondo mientras la interfaz está abierta", true));

    public StringSetting commandPrefix = addSetting(new StringSetting("prefijo", "prefijo de los comandos", "$", 10));
    public StringSetting chatName = addSetting(new StringSetting("nombre", "qué nombre usar en notificaciones por el chat", "smegma", 20));

    public BooleanSetting multipleKeybinds = addSetting(new BooleanSetting("teclas multimódulo", "permitir asignar la misma tecla a más de un módulo", false));

    private int prevRed, prevGreen, prevBlue;

    public GUI() {
        super("interfaz", "ajustes de la interfaz, y otros misceláneos");
        assignKey(GLFW.GLFW_KEY_RIGHT_SHIFT);

        // registrar aquí, porque no se añade a ModuleManager.modules. de lo contrario no podría detectar eventos
        CameraTweaks.EVENT_BUS.register(this);

        keyMode.setVisible(false);
        notify.setVisible(false);

        searchBar.onUpdate(v -> matchCase.setVisible(v));
    }

    @SubscribeEvent
    public void onLoadCommandManager(LoadCommandManagerEvent event) {
        commandPrefix.onUpdate(prefix -> CommandManager.INSTANCE.setPrefix(prefix));
        commandPrefix.setDefaultValue(CommandManager.INSTANCE.getPrefix());

        chatName.onUpdate(name -> {
            ChatUtil.updatePrefix(name);
            ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class).updateClientName();
        });
    }

    // lógica un poco prolija pero bueno
    @SubscribeEvent
    public void onLoadClickGUI(LoadClickGUIEvent event) {
        rainbow.onUpdate(v -> {
            if (v) {
                prevRed = red.getIntValue();
                prevGreen = green.getIntValue();
                prevBlue = blue.getIntValue();
            } else {
                red.setValue(prevRed);
                green.setValue(prevGreen);
                blue.setValue(prevBlue);
            }

            rainbowSpeed.setVisible(v);
            saturation.setVisible(v);
            brightness.setVisible(v);
            red.setVisible(!v);
            green.setVisible(!v);
            blue.setVisible(!v);

            applyRGBAColorUpdates();
        });

        red.onUpdate(r -> applyRGBAColorUpdates());
        green.onUpdate(g -> applyRGBAColorUpdates());
        blue.onUpdate(b -> applyRGBAColorUpdates());
        alpha.onUpdate(a -> applyRGBAColorUpdates());
    }

    @SubscribeEvent
    public void onTick(TickEvent.Post event) {
        if (rainbow.isEnabled()) {
            float speed = (float) (rainbowSpeed.getMax() - rainbowSpeed.getValue());
            float hue = (System.currentTimeMillis() % (long) speed) / speed;
            Color color = Color.getHSBColor(hue, saturation.getFloatValue(), brightness.getFloatValue());

            Colors.red = color.getRed();
            Colors.green = color.getGreen();
            Colors.blue = color.getBlue();

            updateColor();
        }
    }

    private void applyRGBAColorUpdates() {
        Colors.red = red.getIntValue();
        Colors.green = green.getIntValue();
        Colors.blue = blue.getIntValue();
        Colors.alpha = alpha.getIntValue();
        updateColor();
    }

    private void updateColor() {
        Colors.updateAllColors(new Color(
                Colors.red,
                Colors.green,
                Colors.blue,
                Colors.alpha
        ));
    }
}
