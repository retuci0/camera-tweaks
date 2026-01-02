package me.retucio.camtweaks.module.modules.render;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.ColorSetting;

import java.awt.*;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.WorldRendererMixin
 */

public class BlockOutline extends Module {

    public ColorSetting color = addSetting(new ColorSetting("color", "color del contorno de los bloques",
            new Color(0, 0, 0, 102), true));

    public BlockOutline() {
        super("contorno",
                "customiza el contorno de los bloques",
                Category.RENDER);
    }
}
