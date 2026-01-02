package me.retucio.camtweaks.module.modules.player;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;

public class Headhitters extends Module {

    public Headhitters() {
        super("headhitters",
                "mantén pulsado el espacio para hacer headhitting sin tener que espamearlo",
                Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.jumpingCooldown = 0;
    }
}
