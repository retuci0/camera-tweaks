package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;

public class Step extends Module {

    public final NumberSetting height = sgGeneral.add(new NumberSetting(
            "altura", "altura máxima a subir",
            1, 0, 3, 0.1
    ));

    public Step() {
        super("escalones", "hace que el bloque de subida de escalón sea la distancia deseada", Category.MOVEMENT);
    }
}
