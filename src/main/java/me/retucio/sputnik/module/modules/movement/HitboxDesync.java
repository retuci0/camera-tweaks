package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;


/**
 * @link <a href="https://github.com/mioclient/hitbox-desync"></a>
 *
 * @author retucio
 */

public class HitboxDesync extends Module {

    private final double MAGIC_OFFSET = 0.200009968835369999878673424677777777777761;

    public HitboxDesync() {
        super("desync de hitbox", "exploit chino", Category.MOVEMENT);
        keyMode.setDefaultValue(KeyMode.HOLD);
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) return;

        Direction dir = mc.player.getNearestViewDirection();
        AABB box = mc.player.getBoundingBox();
        Vec3 center = box.getCenter();
        Vec3 offset = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
        Vec3 posVec = merge(new Vec3(Math.floor(center.x) + 0.5, Math.floor(center.y), Math.floor(center.z) + 0.5)
                .add(new Vec3(
                        offset.x * MAGIC_OFFSET,
                        offset.y * MAGIC_OFFSET,
                        offset.z * MAGIC_OFFSET
                )), dir);

        mc.player.setPos(posVec.x == 0 ? mc.player.getX() : posVec.x, mc.player.getY(), posVec.z == 0 ? mc.player.getZ() : posVec.z);
        toggle();
    }

    private Vec3 merge(@NonNull Vec3 a, Direction facing) {
        return new Vec3(a.x * Math.abs(facing.getStepX()), a.y * Math.abs(facing.getStepY()), a.z * Math.abs(facing.getStepZ()));
    }
}
