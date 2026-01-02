package me.retucio.camtweaks.module.modules.render;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;

import java.awt.*;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.DamageParticleMixin
 * @see me.retucio.camtweaks.mixin.DamageParticleFactoryMixin
 */

public class CritsPlus extends Module {

    public ColorSetting color = addSetting(new ColorSetting("color", "color de las partículas", new Color(0, 0, 255, 255), false));

    public NumberSetting scale = addSetting(new NumberSetting("escala", "tamaño", 1, 0, 2, 0.05));
    public NumberSetting multiplier;  // no sé dónde se calcula cuántas partículas aparecen :/
    public NumberSetting velocityMultipler = addSetting(new NumberSetting("dispersión", "multiplicador de la velocidad de dispersión", 1, 0, 10, 0.1));
    public NumberSetting gravity = addSetting(new NumberSetting("gravedad", "multiplicador de fuerza de gravedad", 1, 0, 10, 0.1));
    public NumberSetting maxAge = addSetting(new NumberSetting("vida máxima", "cuánto persiste la partícula", 10, 0, 50, 1));
    public BooleanSetting collide = addSetting(new BooleanSetting("colisionar", "colisionar con bloques", false));

    public CritsPlus() {
        super("críticos",
                "cambia la apariencia de los críticos al gusto",
                Category.RENDER);
    }
}
