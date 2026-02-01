package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;

public class TridentBoost extends Module {

    public final NumberSetting multiplier = sgGeneral.add(new NumberSetting(
            "boost",
            "multiplicador del movimiento",
            2,
            0,
            5,
            0.1
    ));

    public final BooleanSetting outOfWater = sgGeneral.add(new BooleanSetting(
            "fuera del agua",
            "permitir el uso de la propulsión acuática fuera del agua",
            true
    ));

    public TridentBoost() {
        super("tenedor mágico", "hermano se piensa que es Poseidón", Category.MOVEMENT);
    }
}
