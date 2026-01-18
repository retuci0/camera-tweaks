package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;

public class Dolphin extends Module {

    public Dolphin() {
        super("delfín", "*ruido de delfín*", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.isSubmergedInWater()) {
            if (!mc.player.isJumping()) mc.player.jump();
        }
    }
}
