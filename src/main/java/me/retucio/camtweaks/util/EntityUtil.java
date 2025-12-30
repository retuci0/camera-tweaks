package me.retucio.camtweaks.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3d;

import static me.retucio.camtweaks.CameraTweaks.mc;

public class EntityUtil {

    public static Vector3d getEntityVector(Vector3d vector, Entity entity, double tickDelta) {
        vector.x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        vector.y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        vector.z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
        return vector;
    }

    public static Entity getEntityPlayerIsLookingAt() {
        if (mc.player == null || mc.world == null) return null;

        float reachDistance = (float) mc.player.getEntityInteractionRange();

        Vec3d cameraPos = mc.player.getCameraPosVec(1.0F);
        Vec3d rotation = mc.player.getRotationVec(1.0F);
        Vec3d endPos = cameraPos.add(rotation.x * reachDistance, rotation.y * reachDistance, rotation.z * reachDistance);

        EntityHitResult entityHitResult = ProjectileUtil.raycast(
                mc.player,
                cameraPos,
                endPos,
                new Box(cameraPos, endPos),
                entity -> !entity.isSpectator() && entity.canHit(),
                reachDistance * reachDistance
        );

        return entityHitResult != null ? entityHitResult.getEntity() : null;
    }

    public static boolean hasLineOfSight(Entity viewer, Entity target) {
        HitResult hitResult = viewer.getEntityWorld().raycast(new RaycastContext(
                viewer.getEyePos(),
                target.getEyePos(),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                viewer
        ));

        if (hitResult.getType() == HitResult.Type.MISS) return true;
        else if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) hitResult).getEntity() == target;
        }
        return false;
    }

    public static double getYaw(Entity entity) {
        return getYaw(entity.getEntityPos());
    }

    public static double getYaw(Vec3d pos) {
        return mc.player.getYaw() + MathHelper.wrapDegrees(
                (float) Math.toDegrees(Math.atan2(
                        pos.getZ() - mc.player.getZ(),
                        pos.getX() - mc.player.getX())
                ) - 90f - mc.player.getYaw());
    }

    public static double getPitch(Vec3d pos) {
        double diffX = pos.getX() - mc.player.getX();
        double diffY = pos.getY() - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = pos.getZ() - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }

    public static double getPitch(Entity entity, Target target) {
        double y;
        if (target == Target.HEAD) y = entity.getEyeY();
        else if (target == Target.BODY) y = entity.getY() + entity.getHeight() / 2;
        else y = entity.getY();

        double diffX = entity.getX() - mc.player.getX();
        double diffY = y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = entity.getZ() - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }

    public static enum Target {
        HEAD,
        BODY,
        FEET;
    }
}
