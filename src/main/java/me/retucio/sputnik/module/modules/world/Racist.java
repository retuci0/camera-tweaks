package me.retucio.sputnik.module.modules.world;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;

import java.util.ArrayList;
import java.util.List;


public class Racist extends Module {

    private int lookTimer;
    private final List<EnderMan> lookedEndermen = new ArrayList<>();

    public Racist() {
        super("racismo",
                "amego segarro",
                Category.WORLD);
    }

    @Override
    public void onDisable() {
        lookedEndermen.clear();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) return;

        for (Entity entity : mc.level.getEntities().getAll()) {
            if (!(entity instanceof EnderMan enderman)) continue;

            if (shouldLookAt(enderman)) {
                lookAtEnderman(enderman);

                lookedEndermen.add(enderman);
                lookTimer = 0;
                return;
            }
        }

        lookTimer++;
    }

    private boolean shouldLookAt(EnderMan enderman) {
        return !(lookedEndermen.contains(enderman)) && lookTimer >= 10
                && EntityUtil.hasLineOfSight(mc.player, enderman);
    }

    private void lookAtEnderman(EnderMan enderman) {
        double yaw = EntityUtil.getYaw(enderman);
        double pitch = EntityUtil.getPitch(enderman, EntityUtil.Target.HEAD);

        mc.player.setYRot((float) yaw);
        mc.player.setXRot((float) pitch);
    }
}
