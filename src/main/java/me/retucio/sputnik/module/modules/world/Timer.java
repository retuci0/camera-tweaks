package me.retucio.sputnik.module.modules.world;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;

public class Timer extends Module {

    public NumberSetting multiplier = sgGeneral.add(new NumberSetting(
            "multi",
            "como en el Balatro",
            1,
            0.1,
            10,
            0.1
    ));

    public Timer() {
        super("cocaína", "cambia la velocidad de todo el en juego", Category.WORLD);
    }
}
