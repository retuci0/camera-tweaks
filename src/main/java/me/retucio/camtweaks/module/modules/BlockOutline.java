package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.WorldRendererMixin
 */

public class BlockOutline extends Module {

    public BooleanSetting rainbow = addSetting(new BooleanSetting("RGB", "erre ge be gueiming", false));
    public NumberSetting rainbowSpeed = addSetting(new NumberSetting("velocidad", "velocidad de la bandera gay", 2000, 0, 10000, 2));

    public NumberSetting red = addSetting(new NumberSetting("rojo", "factor r del rbg", 0, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "factor g del rgb", 0, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "factor b del rbg", 0, 0, 255, 1));

    public NumberSetting alpha = addSetting(new NumberSetting("opacidad", "transparencia invertida", 102, 0, 255, 1));

    public BlockOutline() {
        super("contorno", "customiza el contorno de los bloques");
        rainbow.onUpdate(v -> {
            rainbowSpeed.setVisible(v);
            red.setVisible(!v);
            green.setVisible(!v);
            blue.setVisible(!v);
        });
    }
}
