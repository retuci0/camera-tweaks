package me.retucio.camtweaks.util;

import java.awt.*;

import static me.retucio.camtweaks.ui.frames.ClickGUISettingsFrame.guiSettings;


// clase para los colores, para no repetirme (regla DRY)
// si se ve especificado como color en algún lado "-1", significa blanco
public class Colors {

    public static int frameHeadRed = guiSettings.red.getIntValue();
    public static int frameHeadGreen = guiSettings.green.getIntValue();
    public static int frameHeadBlue = guiSettings.blue.getIntValue();
    public static int frameHeadAlpha = guiSettings.alpha.getIntValue();

    public static int frameHeadColor = new Color(
            frameHeadRed,
            frameHeadGreen,
            frameHeadBlue,
            frameHeadAlpha
    ).getRGB();

    public static int frameBGColor = new Color(60, 60, 60, 70).getRGB();

    public static int enabledHoveredModuleButtonColor = new Color(20, 20, 255, 180).getRGB();
    public static int enabledModuleButtonColor = new Color(0, 0, 255, 180).getRGB();
    public static int hoveredModuleButtonColor = new Color(30, 30, 30, 100).getRGB();
    public static int moduleButtonColor = new Color(40, 40, 40, 75).getRGB();

    public static int hoveredSettingButtonColor = new Color(30, 30, 30, 100).getRGB();
    public static int settingButtonColor = new Color(40, 40, 40, 75).getRGB();
    public static int sliderFillingColor = new Color(60, 120, 220, 180).getRGB();

    // necesitan ser de tipo java.awt.Color para utilizar el método .brighter()
    public static Color enabledToggleButtonColor = new Color(0, 180, 0, 180);
    public static Color disabledToggleButtonColor = new Color(120, 30, 30, 150);

}
