package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.DamageParticleMixin
 * @see me.retucio.camtweaks.mixin.DamageParticleFactoryMixin
 */

public class CritsPlus extends Module {

    public BooleanSetting rainbow = addSetting(new BooleanSetting("LUCES GAMING", "PARTÍCULAS GAMING", false));
    public NumberSetting rainbowSpeed = addSetting(new NumberSetting("velocidad del erre ge be", "qué tan gaming son las luces gaming", 1000, 0, 10000, 2));

    public NumberSetting red = addSetting(new NumberSetting("rojo", "comunismo", 0, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "vox", 0, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "metanfetamina", 255, 0, 255, 1));
    public NumberSetting alpha = addSetting(new NumberSetting("alpha", "opacidad", 255, 0, 255, 1));

    public NumberSetting scale = addSetting(new NumberSetting("escala", "tamaño", 1, 0, 2, 0.05));
    public NumberSetting multiplier;  // no sé dónde se calcula cuántas partículas aparecen :/
    public NumberSetting velocityMultipler = addSetting(new NumberSetting("dispersión", "multiplicador de la velocidad de dispersión", 1, 0, 10, 0.1));
    public NumberSetting gravity = addSetting(new NumberSetting("gravedad", "multiplicador de fuerza de gravedad", 1, 0, 10, 0.1));
    public NumberSetting maxAge = addSetting(new NumberSetting("vida máxima", "cuánto persiste la partícula", 4, 0, 50, 1));
    public BooleanSetting collide = addSetting(new BooleanSetting("colisionar", "colisionar con bloques", false));

    public CritsPlus() {
        super("críticos", "cambia la apariencia de los críticos al gusto");
        rainbow.onUpdate(v -> {
            rainbowSpeed.setVisible(v);
            red.setVisible(!v);
            green.setVisible(!v);
            blue.setVisible(!v);
        });
    }
}
