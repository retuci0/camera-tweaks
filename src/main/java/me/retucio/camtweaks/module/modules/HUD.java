package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.module.settings.StringSetting;

public class HUD extends Module {

    public NumberSetting red = addSetting(new NumberSetting("rojo", "cantidad de rojo", 255, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "cantidad de verde", 255, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "cantidad de azul", 255, 0, 255, 1));

    public BooleanSetting rainbow = addSetting(new BooleanSetting("arcoíris", "gay.", false));
    public NumberSetting rainbowSpeed = addSetting(new NumberSetting("velocidad del arcoíris", "velocidad del arcoíris", 1000, 0, 10000, 2));

    public NumberSetting alpha  = addSetting(new NumberSetting("opacidad", "antitransparencia", 255, 0, 255, 1));
    public BooleanSetting shadow = addSetting(new BooleanSetting("sombra", "texto con sombra", true));

    public BooleanSetting dontOverride = addSetting(new BooleanSetting("no sobreescribir", "evita dibujar por encima de otros elementos de la interfaz", true));

    public EnumSetting<TimeFormat> timeFormat = addSetting(new EnumSetting<>("formato de la hora", "12h o 24h", TimeFormat.class, TimeFormat.TWENTY_FOUR_HOUR));
    public NumberSetting timezone = addSetting(new NumberSetting("zona horaria", "zona horaria en UTC+n", 1, -6, 6, 1));

    // elementos
    public BooleanSetting coords = addSetting(new BooleanSetting("coordenadas", "mostrar coordenadas", true));
    public NumberSetting coordsX = new NumberSetting("X de las coordenadas", ".", -1, -1, 1920, 1);
    public NumberSetting coordsY = new NumberSetting("Y de las coordenadas", ".", -1, -1, 1080, 1);

    public StringSetting customText = addSetting(new StringSetting("texto custom", "marca de agua (dejar vacío para quitar)", "", 40));
    public NumberSetting customTextX = new NumberSetting("X de la marca de agua", ".", -1, -1, 1920, 1);
    public NumberSetting customTextY = new NumberSetting("Y de la marca de agua", ".", -1, -1, 1080, 1);

    public BooleanSetting fps = addSetting(new BooleanSetting("fps", "mostrar frames por segundo", true));
    public NumberSetting fpsX = new NumberSetting("X de los fps", ".", -1, -1, 1920, 1);
    public NumberSetting fpsY = new NumberSetting("Y de los fps", ".", -1, -1, 1080, 1);

    public BooleanSetting tps = addSetting(new BooleanSetting("tps", "mostrar tasa de ticks por segundo del server", true));
    public NumberSetting tpsX = new NumberSetting("X del tps", ".", -1, -1, 1920, 1);
    public NumberSetting tpsY = new NumberSetting("Y del tps", ".", -1, -1, 1080, 1);

    public BooleanSetting time = addSetting(new BooleanSetting("hora", "mostrar hora actual", true));
    public NumberSetting timeX = new NumberSetting("X de la hora", ".", -1, -1, 1920, 1);
    public NumberSetting timeY = new NumberSetting("Y de la hora", ".", -1, -1, 1080, 1);
    public HUD() {
        super("HUD", "superposición de la pantalla con info. adicional");

        rainbow.onUpdate(v -> {
            rainbowSpeed.setVisible(v);
            red.setVisible(!v);
            green.setVisible(!v);
            blue.setVisible(!v);
        });

        time.onUpdate(v -> {
            timeFormat.setVisible(v);
            timezone.setVisible(v);
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
