package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadClickGUIEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadCommandManagerEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadModuleManagerEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.module.settings.StringSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Colors;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static me.retucio.camtweaks.CameraTweaks.LOGGER;
import static me.retucio.camtweaks.util.Colors.*;

// módulo no visible solo para los ajustes de la interfaz (y del cliente en general)
public class GUI extends Module {

    public BooleanSetting rainbow = addSetting(new BooleanSetting("gaming", "gaming", false));
    public NumberSetting rainbowSpeed = addSetting(new NumberSetting("velocidad del gaming", "velocidad, del gaming.",
            1000, 0, 10000, 2));

    public NumberSetting red = addSetting(new NumberSetting("rojo", "cantidad de rojo en el RGB del marco", 0, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "cantidad de verde en el RGB del marco", 0, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "cantidad de azul en el RGB del marco", 255, 0, 255, 1));
    public NumberSetting alpha = addSetting(new NumberSetting("alpha", "opacidad del marco", 180, 0, 255, 1));

    // números negativos para deslizamiento inverso, 0 para desactivar
    public NumberSetting scrollSens = addSetting(new NumberSetting("sensibilidad del scroll", "qué tan sensible es la interfaz a la rueda del ratón",
            5, -15, 15, 0.5));

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
            Color color = Color.getHSBColor(hue, 1f, 1f);

            frameHeadRed = color.getRed();
            frameHeadGreen = color.getGreen();
            frameHeadBlue = color.getBlue();

            updateFrameHeadColor();
        }
    }

    private void applyRGBAColorUpdates() {
        frameHeadRed = red.getIntValue();
        frameHeadGreen = green.getIntValue();
        frameHeadBlue = blue.getIntValue();
        frameHeadAlpha = alpha.getIntValue();
        updateFrameHeadColor();
    }

    private void updateFrameHeadColor() {
        Colors.frameHeadColor = new Color(
                frameHeadRed,
                frameHeadGreen,
                frameHeadBlue,
                frameHeadAlpha
        ).getRGB();
    }
}
