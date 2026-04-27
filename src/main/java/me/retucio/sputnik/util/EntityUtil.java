package me.retucio.sputnik.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import static me.retucio.sputnik.Sputnik.mc;

public class EntityUtil {

    public static Entity getEntityPlayerIsLookingAt() {
        if (mc.player == null || mc.level == null) return null;

        float reachDistance = (float) mc.player.entityInteractionRange();

        Vec3 cameraPos = mc.player.getEyePosition(1.0f);
        Vec3 rotation = mc.player.getViewVector(1.0f);
        Vec3 endPos = cameraPos.add(rotation.x * reachDistance, rotation.y * reachDistance, rotation.z * reachDistance);

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                mc.player,
                cameraPos,
                endPos,
                new AABB(cameraPos, endPos),
                entity -> !entity.isSpectator() && entity.isPickable(),
                reachDistance * reachDistance
        );

        return entityHitResult != null ? entityHitResult.getEntity() : null;
    }

    public static boolean hasLineOfSight(Entity viewer, Entity target) {
        HitResult hitResult = viewer.level().clip(new ClipContext(
                viewer.getEyePosition(),
                target.getEyePosition(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                viewer
        ));

        if (hitResult.getType() == HitResult.Type.MISS) return true;
        else if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) hitResult).getEntity() == target;
        }
        return false;
    }

    public static double getYaw(Entity entity) {
        return getYaw(entity.position());
    }

    public static double getYaw(Vec3 pos) {
        return mc.player.getYRot() + Mth.wrapDegrees(
                (float) Math.toDegrees(Math.atan2(
                        pos.z() - mc.player.getZ(),
                        pos.x() - mc.player.getX())
                ) - 90f - mc.player.getYRot());
    }

    public static double getPitch(Vec3 pos) {
        double diffX = pos.x() - mc.player.getX();
        double diffY = pos.y() - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = pos.z() - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getXRot() + Mth.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getXRot());
    }

    public static double getPitch(Entity entity, Target target) {
        double y;
        if (target == Target.HEAD) y = entity.getEyeY();
        else if (target == Target.BODY) y = entity.getY() + entity.getBbHeight() / 2;
        else y = entity.getY();

        double diffX = entity.getX() - mc.player.getX();
        double diffY = y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = entity.getZ() - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getXRot() + Mth.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getXRot());
    }

    public static void lookAt(Vec3 pos) {
        lookAtClient(pos);
        lookAtServer(pos);
    }

    public static void lookAtClient(Vec3 pos) {
        mc.player.setYRot((float) getYaw(pos));
        mc.player.setXRot((float) getPitch(pos));
    }

    public static void lookAtServer(Vec3 pos) {
        mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                (float) getYaw(pos),
                (float) getPitch(pos),
                mc.player.onGround(),
                mc.player.horizontalCollision
        ));
    }

    public enum Target {
        HEAD,
        BODY,
        FEET;
    }
}
