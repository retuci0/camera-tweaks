package me.retucio.sputnik.module.modules.combat;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Comparator;
import java.util.List;

// https://github.com/etianl/spearHax/blob/main/src/main/java/things/haxHandler.java
public class SpearKill extends Module {

    private final BooleanSetting onlyPlayers = sgGeneral.add(new BooleanSetting(
            "solo jugadores",
            "tener de objetivo solamente jugadores",
            true
    ));


    private final NumberSetting speed = sgGeneral.add(new NumberSetting(
            "velocidad",
            "velocidad del impulso",
            5,
            1,
            15,
            0.2
    ));

    private int heldTicks = 0;
    private Entity crosshairTarget;

    public SpearKill() {
        super("don quijote", "chits para la lanza", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (isHoldingSpear() && mc.options.useKey.isPressed()) {
            heldTicks++;
            if (crosshairTarget == null) crosshairTarget = getTarget();
            if (crosshairTarget == null) return;

            if (!crosshairTarget.isAlive()) return;
            if (!(crosshairTarget instanceof LivingEntity)) return;
            if (onlyPlayers.getValue() && !(crosshairTarget instanceof PlayerEntity)) return;

            Vec3d playerPos = mc.player.getEyePos();
            Vec3d targetPos = crosshairTarget.getBoundingBox().getCenter();
            Vec3d toTarget = targetPos.subtract(playerPos).normalize();

            float yaw = (float) (Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.asin(toTarget.y));

            mc.player.setYaw(yaw);
            mc.player.setHeadYaw(yaw);
            mc.player.setPitch(pitch);

            if (heldTicks >= 10) {
                double lungeSpeed = speed.getValue();
                Vec3d viewDir = Vec3d.fromPolar(mc.player.getPitch(), mc.player.getYaw());
                mc.player.setSprinting(true);
                mc.player.setVelocity(viewDir.multiply(lungeSpeed));
            }
        } else {
            heldTicks = 0;
            crosshairTarget = null;
        }
    }

    private boolean isHoldingSpear() {
        return mc.player.isHolding(
                stack -> stack.streamTags().anyMatch(
                        tag -> tag.equals(ItemTags.SPEARS)
                )
        );
    }

    private Entity getTarget() {
        if (mc.player == null || mc.world == null) return null;

        double maxRange = 256;
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d lookVec = mc.player.getRotationVec(1f);

        HitResult blockHit = mc.world.raycast(new RaycastContext(eyePos,
                eyePos.add(lookVec.multiply(maxRange)), RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        double rayLength = blockHit.getType() == HitResult.Type.MISS ? maxRange :
                eyePos.distanceTo(blockHit.getPos());

        List<Entity> candidates = mc.world.getOtherEntities(mc.player,
                mc.player.getBoundingBox().stretch(lookVec.multiply(rayLength)),
                e -> e instanceof LivingEntity && e.isAlive() && e != mc.player);

        candidates.sort(Comparator.comparingDouble(e ->
                eyePos.squaredDistanceTo(e.getBoundingBox().getCenter())));

        double coneAngle = 0.999;
        for (Entity e : candidates) {
            if (eyePos.distanceTo(e.getBoundingBox().getCenter()) > rayLength) break;
            Vec3d toEntity = e.getBoundingBox().getCenter().subtract(eyePos).normalize();
            if (lookVec.dotProduct(toEntity) > coneAngle &&
                    (!onlyPlayers.getValue() || e instanceof PlayerEntity)) {
                return e;
            }
        }

        return null;
    }
}
