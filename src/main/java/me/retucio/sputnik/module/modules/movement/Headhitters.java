package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;

public class Headhitters extends Module {

    public NumberSetting delay = sgGeneral.add(new NumberSetting("delay", "delay de saltos", 0, 0, 20, 1));

    public Headhitters() {
        super("headhitters",
                "mantén pulsado el espacio para hacer headhitting sin tener que espamearlo",
                Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.jumpingCooldown = delay.getIntValue();
    }
}
