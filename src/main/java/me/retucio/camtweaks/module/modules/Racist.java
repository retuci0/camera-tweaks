package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;

import java.util.ArrayList;
import java.util.List;

public class Racist extends Module {

    private int lookTimer;
    private final List<EndermanEntity> lookedEndermen = new ArrayList<>();

    public Racist() {
        super("racismo", "amego segarro");
    }

    @Override
    public void onDisable() {
        lookedEndermen.clear();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndermanEntity enderman)) continue;

            if (shouldLookAt(enderman)) {
                lookAtEnderman(enderman);

                lookedEndermen.add(enderman);
                lookTimer = 0;
                return;
            }
        }

        lookTimer++;
    }

    private boolean shouldLookAt(EndermanEntity enderman) {
        return !(lookedEndermen.contains(enderman)) && lookTimer >= 10
                && EntityUtil.hasLineOfSight(mc.player, enderman);
    }

    private void lookAtEnderman(EndermanEntity enderman) {
        double yaw = EntityUtil.getYaw(enderman);
        double pitch = EntityUtil.getPitch(enderman, EntityUtil.Target.HEAD);

        mc.player.setYaw((float) yaw);
        mc.player.setPitch((float) pitch);
    }


}
