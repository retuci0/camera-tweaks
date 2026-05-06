package me.retucio.sputnik.module.modules.combat;

import me.retucio.sputnik.friend.FriendManager;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;


/**
 * @link <a href="https://github.com/etianl/spearHax/blob/main/src/main/java/things/haxHandler.java">créditos a etianl</a>
 *
 * @"author" retucio
 */

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

    private final BooleanSetting ignoreFriends = sgGeneral.add(new BooleanSetting(
            "ignorar amigos",
            "no targetear amigos",
            true
    ));

    private int heldTicks = 0;
    private Entity crosshairTarget;

    public SpearKill() {
        super("don quijote", "chits para la lanza", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (isHoldingSpear() && mc.options.keyUse.isDown()) {
            heldTicks++;
            if (crosshairTarget == null) crosshairTarget = getTarget();
            if (crosshairTarget == null) return;

            if (!crosshairTarget.isAlive()) return;
            if (!(crosshairTarget instanceof LivingEntity)) return;
            if (onlyPlayers.getValue() && !(crosshairTarget instanceof Player)) return;

            Vec3 playerPos = mc.player.getEyePosition();
            Vec3 targetPos = crosshairTarget.getBoundingBox().getCenter();
            Vec3 toTarget = targetPos.subtract(playerPos).normalize();

            float yaw = (float) (Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.asin(toTarget.y));

            mc.player.setYRot(yaw);
            mc.player.setYHeadRot(yaw);
            mc.player.setXRot(pitch);

            if (heldTicks >= 10) {
                double lungeSpeed = speed.getValue();
                Vec3 viewDir = Vec3.directionFromRotation(mc.player.getXRot(), mc.player.getYRot());
                mc.player.setSprinting(true);
                mc.player.setDeltaMovement(viewDir.scale(lungeSpeed));
            }
        } else {
            heldTicks = 0;
            crosshairTarget = null;
        }
    }

    private boolean isHoldingSpear() {
        return mc.player.isHolding(
                stack -> stack.tags().anyMatch(
                        tag -> tag.equals(ItemTags.SPEARS)
                )
        );
    }

    private Entity getTarget() {
        if (mc.player == null || mc.level == null) return null;

        double maxRange = 256;
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getViewVector(1f);

        HitResult blockHit = mc.level.clip(new ClipContext(eyePos,
                eyePos.add(lookVec.scale(maxRange)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, mc.player));
        double rayLength = blockHit.getType() == HitResult.Type.MISS ? maxRange :
                eyePos.distanceTo(blockHit.getLocation());

        List<Entity> candidates = mc.level.getEntities(mc.player,
                mc.player.getBoundingBox().expandTowards(lookVec.scale(rayLength)),
                e -> e instanceof LivingEntity
                        && e.isAlive() && e != mc.player
                        && (!ignoreFriends.getValue() || FriendManager.INSTANCE.isFriend(e)));

        candidates.sort(Comparator.comparingDouble(e ->
                eyePos.distanceToSqr(e.getBoundingBox().getCenter())));

        double coneAngle = 0.999;
        for (Entity entity : candidates) {
            if (eyePos.distanceTo(entity.getBoundingBox().getCenter()) > rayLength) break;
            Vec3 toEntity = entity.getBoundingBox().getCenter().subtract(eyePos).normalize();
            if (lookVec.dot(toEntity) > coneAngle &&
                    (!onlyPlayers.getValue() || entity instanceof Player)) {
                return entity;
            }
        }

        return null;
    }
}
