package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.module.settings.StringSetting;
import me.retucio.camtweaks.util.Colors;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static me.retucio.camtweaks.CameraTweaks.commandManager;
import static me.retucio.camtweaks.util.Colors.*;

public class GUI extends Module {

    public NumberSetting red = new NumberSetting("rojo", "cantidad de rojo en el RGB del marco", 0, 0, 255, 1);
    public NumberSetting green = new NumberSetting("verde", "cantidad de verde en el RGB del marco", 0, 0, 255, 1);
    public NumberSetting blue = new NumberSetting("azul", "cantidad de azul en el RGB del marco", 255, 0, 255, 1);
    public NumberSetting alpha = new NumberSetting("alpha", "opacidad del marco", 180, 0, 255, 1);

    public NumberSetting scrollSens = new NumberSetting("sensibilidad del scroll", "qué tan sensible es la interfaz a la rueda del ratón",
            5, 0, 15, 0.5);

    public StringSetting commandPrefix = new StringSetting("prefijo", "prefijo de los comandos", commandManager.getPrefix(), 10);

    public GUI() {
        super("interfaz", "ajustes de la interfaz, y otros misceláneos");
        addSettings(red, green, blue, alpha, scrollSens, commandPrefix);
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);

        keyMode.setVisible(false);
        notify.setVisible(false);

        commandPrefix.onUpdate(prefix -> commandManager.setPrefix(prefix));
        red.onUpdate(r -> {frameHeadRed = r.intValue(); updateFrameHeadColor();});
        green.onUpdate(g -> {frameHeadGreen = g.intValue(); updateFrameHeadColor();});
        blue.onUpdate(b -> {frameHeadBlue = b.intValue(); updateFrameHeadColor();});
        alpha.onUpdate(a -> {frameHeadAlpha = a.intValue(); updateFrameHeadColor();});
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
