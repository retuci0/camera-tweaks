package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ReverseStep extends Module {

    public NumberSetting height = sgGeneral.add(new NumberSetting(
            "altura", "altura máxima a bajar",
            1, 0, 20, 0.1
    ));

    public NumberSetting velocity = sgGeneral.add(new NumberSetting(
            "velocidad", "velocidad a la que caer en bps",
            1, 0, 10, 0.1
    ));

    public BooleanSetting jumping = sgGeneral.add(new BooleanSetting(
            "saltar", "permitir saltar",
            true
    ));

    public BooleanSetting disableInWater = sgGeneral.add(new BooleanSetting(
            "desactivar en agua", ".",
            true
    ));

    public ReverseStep() {
        super("escalones inversos",
                "escalones pero pabajo",
                Category.PLAYER
        );
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (!mc.player.isOnGround()) {
            if (jumping.getValue()) return;
        }

        if ((mc.player.isTouchingWater() || mc.player.isInLava())) {
            if (disableInWater.getValue()) return;
        }

        double dropHeight = getHeight();
        if (dropHeight > 0 && dropHeight <= height.getValue()) {
            mc.player.addVelocity(0, -height.getValue() + mc.player.getVelocity().getY(), 0);
        }
    }

    private double getHeight() {
        Vec3d start = new Vec3d(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
        Vec3d end = new Vec3d(mc.player.getX(), mc.player.getY() - height.getValue() - 1, mc.player.getZ());

        RaycastContext context = new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.ANY,
                mc.player
        );

        HitResult hit = mc.world.raycast(context);

        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = new BlockPos(
                    (int) hit.getPos().x,
                    (int) hit.getPos().y,
                    (int) hit.getPos().z
            );

            return mc.player.getY() - (hitPos.getY());
        }

        return -1;
    }
}
