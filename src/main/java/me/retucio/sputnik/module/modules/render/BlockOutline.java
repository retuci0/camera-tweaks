package me.retucio.sputnik.module.modules.render;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.render.RenderBlockOutlineEvent;
import me.retucio.sputnik.mixin.mixins.render.LevelRendererMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.Colors;

/** continúa en:
 * @see LevelRendererMixin
 */

public class BlockOutline extends Module {

    private final ColorSetting color = sgGeneral.add(new ColorSetting(
            "color",
            "color del contorno de los bloques",
            Colors.mainColor,
            true)
    );

    private final NumberSetting lineWidth = sgGeneral.add(new NumberSetting(
            "grosor",
            "grosor de las líneas",
            1,
            0.1,
            10,
            0.1
    ));

    public BlockOutline() {
        super("contorno",
                "customiza el contorno de los bloques",
                Category.RENDER);
    }

    @EventListener
    private void onRenderBlockOutline(RenderBlockOutlineEvent event) {
        event.setColor(color.getRGB());
        event.setLineWidth(lineWidth.getFloatValue());
    }
}
