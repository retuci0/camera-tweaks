package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.module.settings.StringSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Colors;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static me.retucio.camtweaks.util.Colors.*;

// módulo no visible solo para los ajustes de la interfaz (y del cliente en general)
public class GUI extends Module {

    public NumberSetting red = addSetting(new NumberSetting("rojo", "cantidad de rojo en el RGB del marco", 0, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "cantidad de verde en el RGB del marco", 0, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "cantidad de azul en el RGB del marco", 255, 0, 255, 1));
    public NumberSetting alpha = addSetting(new NumberSetting("alpha", "opacidad del marco", 180, 0, 255, 1));

    public NumberSetting scrollSens = addSetting(new NumberSetting("sensibilidad del scroll", "qué tan sensible es la interfaz a la rueda del ratón",
            5, 0, 15, 0.5));

    public StringSetting commandPrefix = addSetting(new StringSetting("prefijo", "prefijo de los comandos", "$", 10));
    public StringSetting chatName = addSetting(new StringSetting("nombre", "qué nombre usar en notificaciones por el chat", "smegma", 20));

    public BooleanSetting multipleKeybinds = addSetting(new BooleanSetting("teclas multimódulo", "permitir asignar la misma tecla a más de un módulo", false));

    public GUI() {
        super("interfaz", "ajustes de la interfaz, y otros misceláneos");
        assignKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
        keyMode.setVisible(false);
        notify.setVisible(false);

        commandPrefix.onUpdate(prefix -> CommandManager.INSTANCE.setPrefix(prefix));
        red.onUpdate(r -> {frameHeadRed = r.intValue(); updateFrameHeadColor();});
        green.onUpdate(g -> {frameHeadGreen = g.intValue(); updateFrameHeadColor();});
        blue.onUpdate(b -> {frameHeadBlue = b.intValue(); updateFrameHeadColor();});
        alpha.onUpdate(a -> {frameHeadAlpha = a.intValue(); updateFrameHeadColor();});

        if (mc != null) mc.execute(() -> commandPrefix.setDefaultValue(CommandManager.INSTANCE.getPrefix()));

        chatName.onUpdate(name -> {
            ChatUtil.updatePrefix(name);
            ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class).updateClientName();
        });
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
