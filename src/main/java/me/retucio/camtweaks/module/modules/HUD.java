package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.*;
import org.lwjgl.glfw.GLFW;

/** lógica del HUD manejada en:
 * @see me.retucio.camtweaks.ui.HudRenderer
 * @see me.retucio.camtweaks.ui.widgets.HudElement
 * @see me.retucio.camtweaks.ui.HudEditorScreen
 */

public class HUD extends Module {

    // editor
    public KeySetting editorKey = addSetting(new KeySetting("tecla del editor", "tecla asignada al editor de elementos del hud", GLFW.GLFW_KEY_PAGE_UP));

    // colores
    public NumberSetting red = addSetting(new NumberSetting("rojo", "cantidad de rojo", 255, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "cantidad de verde", 255, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "cantidad de azul", 255, 0, 255, 1));

    public BooleanSetting rainbow = addSetting(new BooleanSetting("arcoíris", "gay.", false));
    public NumberSetting rainbowSpeed = addSetting(new NumberSetting("velocidad del arcoíris", "velocidad del arcoíris", 1000, 0, 10000, 2));

    public NumberSetting alpha  = addSetting(new NumberSetting("opacidad", "antitransparencia", 255, 0, 255, 1));

    // ajustes
    public BooleanSetting showOnF3 = addSetting(new BooleanSetting("mostrar en F3", "renderizar HUD en el menú de debug", false));
    public BooleanSetting shadow = addSetting(new BooleanSetting("sombra", "texto con sombra", true));
    public NumberSetting timezone = addSetting(new NumberSetting("zona horaria", "zona horaria en UTC+n", 1, -6, 6, 1));
    public EnumSetting<TimeFormat> timeFormat = addSetting(new EnumSetting<>("formato de la hora", "12h o 24h", TimeFormat.class, TimeFormat.TWENTY_FOUR_HOUR));
    public StringSetting customText = addSetting(new StringSetting("texto custom", "marca de agua (dejar vacío para quitar)", "adolf jitler inshtagram feishbuc twiter", 40));

    public HUD() {
        super("HUD", "superposición de la pantalla con info. adicional");

        rainbow.onUpdate(v -> {
            red.setVisible(!v);
            green.setVisible(!v);
            blue.setVisible(!v);
            rainbowSpeed.setVisible(v);
        });
    }

    public enum TimeFormat {
        TWENTY_FOUR_HOUR("24h"),
        TWELVE_HOUR("12h");

        final String name;
        TimeFormat(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
