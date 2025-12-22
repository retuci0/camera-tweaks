package me.retucio.camtweaks.util;

import net.minecraft.util.Formatting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public static Color hudEditorScreenBackgroundColor = new Color(0, 0, 0, 120);
    public static Color instructionsTextColor = new Color(190, 190, 190, 255);
    public static Color enabledToggleButtonColor;
    public static Color disabledToggleButtonColor;
    public static Color visibleHudElementColor;
    public static Color disabledHudElementColor;
    public static Color selectedHudElementOutlineColor;
    public static Color unselectedHudElementOutlineColor;

    public static Color RED = new Color(152, 36, 34);
    public static Color ORANGE = new Color(241, 114, 15);
    public static Color YELLOW = new Color(249, 196, 35);
    public static Color LIME = new Color(110, 185, 24);
    public static Color GREEN = new Color(83, 107, 29);
    public static Color CYAN = new Color(22, 133, 144);
    public static Color CELESTE = new Color(57, 177, 215);
    public static Color BLUE = new Color(49, 52, 152);
    public static Color PURPLE = new Color(113, 37, 166);
    public static Color MAGENTA = new Color(183, 61, 172);
    public static Color PINK = new Color(239, 135, 166);
    public static Color LAVENDER = new Color(142, 108, 142);
    public static Color WHITE = new Color(225, 230, 230);
    public static Color SILVER = new Color(137, 137, 128);
    public static Color GRAY = new Color(60, 65, 68);
    public static Color BLACK = new Color(31, 31, 35);
    public static Color BROWN = new Color(113, 70, 39);

    static {
        updateAllColors(new Color(red, green, blue, alpha));
    }

    public static int rainbowInt(int rainbowSpeed, int alpha) {
        float speed = 10001 - rainbowSpeed;  // 10001 para evitar divisiones por cero
        float hue = (System.currentTimeMillis() % (int) speed) / speed;
        Color gamingProMax = Color.getHSBColor(hue, 1, 1);

        return new Color(
                gamingProMax.getRed(),
                gamingProMax.getGreen(),
                gamingProMax.getBlue(),
                alpha
        ).getRGB();
    }

    public static Color rainbowColor(int rainbowSpeed, int alpha) {
        float speed = 10001 - rainbowSpeed;  // 10001 para evitar divisiones por cero
        float hue = (System.currentTimeMillis() % (int) speed) / speed;
        Color gamingProMax = Color.getHSBColor(hue, 1, 1);

        return new Color(
                gamingProMax.getRed(),
                gamingProMax.getGreen(),
                gamingProMax.getBlue(),
                alpha
        );
    }

    public static Color rainbowColor(int rainbowSpeed, int alpha, float saturation, float brightness) {
        float speed = 10001 - rainbowSpeed;  // 10001 para evitar divisiones por cero
        float hue = (System.currentTimeMillis() % (int) speed) / speed;
        Color gamingProMax = Color.getHSBColor(hue, saturation, brightness);

        return new Color(
                gamingProMax.getRed(),
                gamingProMax.getGreen(),
                gamingProMax.getBlue(),
                alpha
        );
    }

    public static void updateAllColors(Color color) {
        mainColor = color;
        enabledToggleButtonColor = mixWithMainColor(new Color(10, 150, 10), 0.8f);
        disabledToggleButtonColor = mixWithMainColor(new Color(150, 10, 10), 0.8f);

        selectedHudElementOutlineColor = color;
        unselectedHudElementOutlineColor = color.darker().darker();

        visibleHudElementColor = new Color(
                enabledToggleButtonColor.getRed(),
                enabledToggleButtonColor.getGreen(),
                enabledToggleButtonColor.getBlue(),
                enabledToggleButtonColor.getAlpha() / 2
        );

        disabledHudElementColor = new Color(
                disabledToggleButtonColor.getRed(),
                disabledToggleButtonColor.getGreen(),
                disabledToggleButtonColor.getBlue(),
                disabledToggleButtonColor.getAlpha() / 2
        );

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
