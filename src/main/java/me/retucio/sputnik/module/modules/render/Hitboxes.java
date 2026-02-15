package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.Colors;

public class Hitboxes extends Module {

    public final ColorSetting color = sgGeneral.add(new ColorSetting(
            "color",
            "color del que renderizar las cajas de los huevos",
            Colors.WHITE,
            true
    ));

    public final NumberSetting lineWidth = sgGeneral.add(new NumberSetting(
            "grosor",
            "grosor de las líneas",
            1,
            0.1,
            10,
            0.1
    ));

    public Hitboxes() {
        super("hitboxes", "modifica cómo se renderizan las hitboxes con F3+B", Category.RENDER);
    }
}
