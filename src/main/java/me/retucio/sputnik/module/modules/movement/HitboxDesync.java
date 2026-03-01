package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;


/**
 * @link <a href="https://github.com/mioclient/hitbox-desync"></a>
 * @author retucio
 */

public class HitboxDesync extends Module {

    private final double MAGIC_OFFSET = 0.200009968835369999878673424677777777777761;

    public HitboxDesync() {
        super("desync de hitbox", "exploit chino", Category.MOVEMENT);
        keyMode.setDefaultValue(KeyModes.HOLD);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        Direction dir = mc.player.getFacing();
        Box box = mc.player.getBoundingBox();
        Vec3d center = box.getCenter();
        Vec3d offset = new Vec3d(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
        Vec3d posVec = merge(new Vec3d(Math.floor(center.x) + 0.5, Math.floor(center.y), Math.floor(center.z) + 0.5)
                .add(new Vec3d(
                        offset.x * MAGIC_OFFSET,
                        offset.y * MAGIC_OFFSET,
                        offset.z * MAGIC_OFFSET
                )), dir);

        mc.player.setPos(posVec.x == 0 ? mc.player.getX() : posVec.x, mc.player.getY(), posVec.z == 0 ? mc.player.getZ() : posVec.z);
        toggle();
    }

    private Vec3d merge(Vec3d a, Direction facing) {
        return new Vec3d(a.x * Math.abs(facing.getOffsetX()), a.y * Math.abs(facing.getOffsetY()), a.z * Math.abs(facing.getOffsetZ()));
    }
}
