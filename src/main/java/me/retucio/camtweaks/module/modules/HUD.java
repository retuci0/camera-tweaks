package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.*;
import me.retucio.camtweaks.ui.screen.HudEditorScreen;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

/** lógica del HUD manejada en:
 * @see me.retucio.camtweaks.ui.HudRenderer
 * @see me.retucio.camtweaks.ui.widgets.HudElement
 * @see HudEditorScreen
 */

public class HUD extends Module {

    // editor
    public KeySetting editorKey = addSetting(new KeySetting("tecla del editor", "tecla asignada al editor de elementos del hud", GLFW.GLFW_KEY_PAGE_UP));

    // ajustes
    public ColorSetting color = addSetting(new ColorSetting("color", "color del texto de los elementos del HUD",
            new Color(255, 255, 255, 255), false));
    public BooleanSetting showOnF3 = addSetting(new BooleanSetting("mostrar en F3", "renderizar HUD en el menú de debug", false));
    public BooleanSetting showOnChat = addSetting(new BooleanSetting("mostrar en chat", "renderizar HUD en la pantalla del chat", false));

    public BooleanSetting shadow = addSetting(new BooleanSetting("sombra", "texto con sombra", true));
    public NumberSetting timezone = addSetting(new NumberSetting("zona horaria", "zona horaria en UTC+n", 1, -6, 6, 1));
    public EnumSetting<TimeFormat> timeFormat = addSetting(new EnumSetting<>("formato de la hora", "12h o 24h", TimeFormat.class, TimeFormat.TWENTY_FOUR_HOUR));
    public StringSetting customText = addSetting(new StringSetting("texto custom", "marca de agua (dejar vacío para quitar)", "adolf jitler inshtagram feishbuc twiter", 40));

    public HUD() {
        super("HUD", "superposición de la pantalla con info. adicional");
    }

    public enum TimeFormat {
        TWENTY_FOUR_HOUR("24h"),
        TWELVE_HOUR("12h");

        final String name;
        TimeFormat(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
