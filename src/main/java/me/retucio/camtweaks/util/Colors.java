package me.retucio.camtweaks.util;

import net.minecraft.util.Formatting;

import java.awt.*;

import static me.retucio.camtweaks.ui.frames.ClientSettingsFrame.guiSettings;


// clase para los colores
// si se ve especificado como color en algún lado "-1", significa blanco
public class Colors {

    public static int red = guiSettings.red.getIntValue();
    public static int green = guiSettings.green.getIntValue();
    public static int blue = guiSettings.blue.getIntValue();
    public static int alpha = guiSettings.alpha.getIntValue();

    public static Color mainColor;
    public static Color frameBGColor = new Color(40, 40, 40, 75);
    public static Color buttonColor = new Color(75, 75, 75, 100);
    public static Color enabledToggleButtonColor;
    public static Color disabledToggleButtonColor;

    static {
        updateAllColors(new Color(red, green, blue, alpha));
    }

    public static void updateAllColors(Color color) {
        mainColor = color;
        enabledToggleButtonColor = mixWithMainColor(new Color(10, 150, 10), 0.8f);
        disabledToggleButtonColor = mixWithMainColor(new Color(150, 10, 10), 0.8f);

        ChatUtil.updatePrefix(ChatUtil.getJustPrefix());
    }

    public static Color mixWithMainColor(Color color, float ratio) {
        return mix(mainColor, color, ratio);
    }

    public static Color mix(Color c1, Color c2, float ratio) {
        float r = c1.getRed() * (1 - ratio) + c2.getRed() * ratio;
        float g = c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio;
        float b = c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio;
        return new Color((int) r, (int) g, (int) b, alpha);
    }

    public static Formatting getFormatting(Color color) {
        return nearest(color);
    }

    public static Formatting nearest(Color input) {
        Formatting nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Formatting formatting : Formatting.values()) {
            if (formatting.getColorValue() == null) continue; // saltarse los modificadores

            Color candidate = new Color(formatting.getColorValue());
            double dist = colorDistance(input, candidate);

            if (dist < minDistance) {
                minDistance = dist;
                nearest = formatting;
            }
        }
        return nearest;
    }

    private static double colorDistance(Color c1, Color c2) {
        int rDiff = c1.getRed() - c2.getRed();
        int gDiff = c1.getGreen() - c2.getGreen();
        int bDiff = c1.getBlue() - c2.getBlue();
        return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    }
}
