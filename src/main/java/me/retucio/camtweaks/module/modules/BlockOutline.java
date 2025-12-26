package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;

import java.awt.*;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.WorldRendererMixin
 */

public class BlockOutline extends Module {

    public ColorSetting color = addSetting(new ColorSetting("color", "color del contorno de los bloques",
            new Color(0, 0, 0, 102), true));

    public BlockOutline() {
        super("contorno", "customiza el contorno de los bloques");
    }
}
