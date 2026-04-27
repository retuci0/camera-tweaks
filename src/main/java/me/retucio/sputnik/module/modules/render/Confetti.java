package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.util.Colors;


/**
 * continúa en:
 * @see me.retucio.sputnik.mixin.mixins.world.TotemParticleMixin
 */

public class Confetti extends Module {

    public ColorSetting color1 = sgGeneral.add(new ColorSetting(
            "color 1",
            "en vez de amarillo",
            Colors.BLACK,
            false
    ));

    public ColorSetting color2 = sgGeneral.add(new ColorSetting(
            "color 2",
            "en vez de verde",
            Colors.mainColor,
            true
    ));

    public Confetti() {
        super("confetti", "cambia el color de las partículas del pop del tótem", Category.RENDER);
    }
}
