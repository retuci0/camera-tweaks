package me.retucio.sputnik.module.modules.camera;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;

import java.awt.*;


/** continúa en:
 * @see me.retucio.sputnik.mixin.mixins.hud.GuiMixin#onRenderCrosshair
 *
 * @author retucio
 */

public class CrosshairPlus extends Module {

    public final ColorSetting color = sgGeneral.add(new ColorSetting(
            "color",
            "color de la mira",
            new Color(255, 255, 255, 255),
            false
    ));

    public final NumberSetting width = sgGeneral.add(new NumberSetting(
            "anchura",
            "anchura de la mira",
            15,
            1,
            150,
            1
    ));

    public final NumberSetting height = sgGeneral.add(new NumberSetting(
            "altura",
            "altura de la mira",
            15,
            1,
            150,
            1
    ));

    public CrosshairPlus() {
        super("mira plus", "customiza la mira (crosshair)", Category.CAMERA);
    }
}
