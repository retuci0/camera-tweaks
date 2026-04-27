package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class ReverseStep extends Module {

    private final NumberSetting height = sgGeneral.add(new NumberSetting(
            "altura", "altura máxima a bajar",
            1, 0, 20, 0.1
    ));

    private final NumberSetting velocity = sgGeneral.add(new NumberSetting(
            "velocidad", "velocidad a la que caer",
            1, 0, 10, 0.1
    ));

    private final BooleanSetting jumping = sgGeneral.add(new BooleanSetting(
            "saltar", "permitir saltar",
            true
    ));

    private final BooleanSetting disableInWater = sgGeneral.add(new BooleanSetting(
            "desactivar en agua", ".",
            true
    ));

    public ReverseStep() {
        super("escalones inversos",
                "escalones pero pabajo",
                Category.MOVEMENT
        );
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (!mc.player.onGround()) {
            if (jumping.getValue()) return;
        }

        if ((mc.player.isInWater() || mc.player.isInLava())) {
            if (disableInWater.getValue()) return;
        }

        double dropHeight = getHeight();
        if (dropHeight > 0 && dropHeight <= height.getValue()) {
            double dy = (-height.getValue() + mc.player.getDeltaMovement().y()) * velocity.getValue();
            mc.player.push(0, dy, 0);
        }
    }

    private double getHeight() {
        Vec3 start = new Vec3(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
        Vec3 end = new Vec3(mc.player.getX(), mc.player.getY() - height.getValue() - 1, mc.player.getZ());

        ClipContext context = new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                mc.player
        );

        HitResult hit = mc.level.clip(context);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = new BlockPos(
                    (int) hit.getLocation().x,
                    (int) hit.getLocation().y,
                    (int) hit.getLocation().z
            );

            return mc.player.getY() - (hitPos.getY());
        }

        return -1;
    }
}
