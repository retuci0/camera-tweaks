package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.KeySetting;
import org.lwjgl.glfw.GLFW;

public class TooltipsPlus extends Module {

    public EnumSetting<DisplayModes> displayMode = addSetting(new EnumSetting<>(
            "mostrar", "cúando mostrar la cajita de los huevos", DisplayModes.class, DisplayModes.ALWAYS));

    public KeySetting displayKey = addSetting(new KeySetting(
            "tecla de visualización", "tecla a presionar para mostrar la cajita", GLFW.GLFW_KEY_LEFT_CONTROL));

    public BooleanSetting middleClickPreview = addSetting(new BooleanSetting("preview de middle click",
            "abre una interfaz con los contenidos de un bloque de almacenamiento al presionar el middle click (la ruedita)", false));

    public EnumSetting<SizeFormat> itemSize = addSetting(new EnumSetting<>(
            "tamaño del ítem", "cómo mostrar el tamaño del ítem en memoria", SizeFormat.class, SizeFormat.NONE));



    public TooltipsPlus() {
        super("tooltips plus", "mejoras varias a las cajitas de descripción de items");
    }

    public enum DisplayModes {
        ALWAYS("siempre"),
        KEY("al presionar una tecla"),
        NEVER("nunca");

        final String name;
        DisplayModes(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    public enum SizeFormat {
        NONE("no mostrar"),
        BYTES("bytes"),
        KB("kilobytes"),
        MB("megabytes");

        final String name;
        SizeFormat(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }


}
