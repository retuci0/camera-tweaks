package me.retucio.sputnik.module.modules.client;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.*;
import me.retucio.sputnik.ui.hud.HudElement;
import me.retucio.sputnik.ui.hud.HudRenderer;
import me.retucio.sputnik.ui.hud.HudEditorScreen;
import me.retucio.sputnik.ui.hud.elements.DynoElement;
import org.lwjgl.glfw.GLFW;

import java.awt.*;


/** lógica del HUD manejada en:
 * @see HudRenderer
 * @see HudElement
 * @see HudEditorScreen
 *
 * @author retucio
 */

public class Hud extends Module {

    private final SettingGroup sgEditor = addSg(new SettingGroup("editor", true));
    private final SettingGroup sgDisplay = addSg(new SettingGroup("visualización", true));
    private final SettingGroup sgElements = addSg(new SettingGroup("elementos", false));

    // editor
    public KeySetting editorKey = sgEditor.add(new KeySetting(
            "tecla del editor",
            "tecla asignada al editor de elementos del hud",
            GLFW.GLFW_KEY_INSERT
    ));

    public BooleanSetting axialMovement = sgEditor.add(new BooleanSetting(
            "movimiento axial",
            "te permite mover los elementos sobre los ejes más fácilmente",
            true
    ));

    public NumberSetting axisOffset = sgEditor.add(new NumberSetting(
            "offset de los ejes",
            "offset a tener en cuenta al usar el movimiento axial",
            10,
            0,
            50,
            1
    )).visibility(axialMovement::getValue);

    public BooleanSetting arrowMovement = sgEditor.add(new BooleanSetting(
            "movimiento con flechas",
            "te permite mover el elemento seleccionado con las teclas de las flechas",
            true
    ));

    public NumberSetting arrowOffset = sgEditor.add(new NumberSetting(
            "cuánto mover con las flechas",
            "cuántos píxeles mover el elemento seleccionado al usar las flechas",
            2,
            1,
            20,
            1
    )).visibility(arrowMovement::getValue);

    // renderizado
    public ColorSetting color = sgDisplay.add(new ColorSetting(
            "color",
            "color del texto de los elementos del HUD",
            new Color(255, 255, 255, 255), false)
    );

    public BooleanSetting shadow = sgDisplay.add(new BooleanSetting(
            "sombra",
            "texto con sombra",
            true
    ));

    public BooleanSetting showOnF3 = sgDisplay.add(new BooleanSetting(
            "mostrar en F3",
            "renderizar HUD en el menú de debug",
            false
    ));

    public BooleanSetting showOnChat = sgDisplay.add(new BooleanSetting(
            "mostrar en chat",
            "renderizar HUD en la pantalla del chat",
            false
    ));

    // elementos
    public NumberSetting timezone = sgElements.add(new NumberSetting(
            "zona horaria",
            "zona horaria en UTC+n",
            1,
            -6,
            6,
            1
    ));

    public EnumSetting<TimeFormat> timeFormat = sgElements.add(new EnumSetting<>(
            "formato de la hora",
            "12h o 24h",
            TimeFormat.class,
            TimeFormat.TWENTY_FOUR_HOUR
    ));

    public StringSetting customText = sgElements.add(new StringSetting(
            "texto custom",
            "marca de agua (dejar vacío para quitar)",
            "adolf jitler inshtagram feishbuc twiter",
            40
    ));

    public EnumSetting<CoordsMode> coordsMode = sgElements.add(new EnumSetting<>(
            "modo de coordenadas",
            "qué coordenadas mostrar",
            CoordsMode.class,
            CoordsMode.OVERWORLD
    ));

    public EnumSetting<Dynosaurs> dyno = sgElements.add(new EnumSetting<>(
            "dinosaurio",
            "qué dinosaurio mostrar",
            Dynosaurs.class, Dynosaurs.SPINOSAURUS
    ));

    public Hud() {
        super("HUD",
                "superposición de la pantalla con info. adicional",
                Category.CLIENT,
                GLFW.GLFW_KEY_F12
        );

        dyno.onUpdate(v -> {
            DynoElement element = (DynoElement) HudRenderer.getElement(DynoElement.class);
            if (element != null) element.reloadTexture();
        });
    }

    public enum TimeFormat {
        TWENTY_FOUR_HOUR("24h"),
        TWELVE_HOUR("12h");

        final String name;
        TimeFormat(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    public enum CoordsMode {
        OVERWORLD("superficie"),
        NETHER("nether"),
        BOTH("ambas");

        private final String name;
        CoordsMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    public enum Dynosaurs {
        ANKYLOSAURUS("anquilosaurio"),
        PTERODACTYL("ptetodáctilo"),
        SPINOSAURUS("espinosaurio"),
        TREX("t-rex"),
        TRICERATOPS("tricerátops"),
        VELOCIRRAPTOR("velocirráptor");

        private final String name;
        Dynosaurs(String name) { this.name = name; }
        @Override public String toString() { return name; }
        public String toRealString() { return super.toString(); }
    }
}
