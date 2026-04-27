package me.retucio.sputnik.module.modules.combat;

import com.github.retucio.neutrino.EventListener;
import com.mojang.blaze3d.vertex.PoseStack;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.*;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.misc.ProjectileInfo;
import me.retucio.sputnik.util.render.RenderUtil;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * @link <a href="https://github.com/maDU59/ProjectilesTrajectoryPreview">créditos a maDU59</a>
 *
 * @"author" retucio
 */

public class ProjectileTrajectories extends Module {

    private final SettingGroup sgTrajectory = addSg(new SettingGroup("trayectoria", true));
    private final SettingGroup sgOutline = addSg(new SettingGroup("contorno", true));
    private final SettingGroup sgFilling = addSg(new SettingGroup("resaltado", true));

    private final BooleanSetting offhand = sgGeneral.add(new BooleanSetting(
            "mano secundaria",
            "predecir trayectoria de proyectiles de la mano secundaria",
            true
    ));

    private final ColorSetting trajColor = sgTrajectory.add(new ColorSetting(
            "color",
            "color de la trayectoria",
            Color.RED,
            false
    ));

    private final NumberSetting trajWidth = sgTrajectory.add(new NumberSetting(
            "grosor",
            "grosor de línea de la trayectoria",
            2,
            0.1,
            10,
            0.1
    ));

    private final EnumSetting<TrajectoryStyle> trajStyle = sgTrajectory.add(new EnumSetting<>(
            "estilo",
            "estilo visual de la trayectoria",
            TrajectoryStyle.class,
            TrajectoryStyle.SOLID
    ));

    private final BooleanSetting impactPoint = sgTrajectory.add(new BooleanSetting(
            "punto de impacto",
            "mostrar el punto de impacto",
            true
    ));

    private final EnumSetting<TrajectoryTargets> outlineTargets = sgOutline.add(new EnumSetting<>(
            "contorno",
            "a qué objetivos renderizar el contorno de la caja",
            TrajectoryTargets.class,
            TrajectoryTargets.BOTH
    ));

    private final ColorSetting outlineColor = sgOutline.add(new ColorSetting(
            "color ",
            "color del contorno",
            Colors.withAlpha(Colors.RED, 200),
            false
    ));

    private final NumberSetting outlineWidth = sgOutline.add(new NumberSetting(
            "grosor",
            "grosor de las líneas del contorno",
            2, 0.1, 10, 0.1
    ));

    private final EnumSetting<TrajectoryTargets> fillingTargets = sgFilling.add(new EnumSetting<>(
            "relleno",
            "a qué objetivos renderizar el relleno de la caja",
            TrajectoryTargets.class,
            TrajectoryTargets.BOTH
    ));

    private final ColorSetting fillingColor = sgFilling.add(new ColorSetting(
            "color  ",
            "color del contorno",
            Colors.withAlpha(Colors.RED.brighter(), 100),
            false
    ));

    public ProjectileTrajectories() {
        super("trayectoria", "muestra la trayectoria de los proyectiles", Category.COMBAT);

        fillingTargets.onUpdate(v -> fillingColor.visibility(v != TrajectoryTargets.NONE));
        outlineTargets.onUpdate(v -> {
            outlineColor.visibility(v != TrajectoryTargets.NONE);
            outlineWidth.visibility(v != TrajectoryTargets.NONE);
        });
    }

    @EventListener
    public void onRenderWorld(Render3DEvent event) {
        if (mc.player == null) return;
        renderProjectileTrajectory(event.getMatrices());
    }

    private void renderProjectileTrajectory(PoseStack matrices) {
        ItemStack mainHand = mc.player.getMainHandItem();
        int handMultiplier = getHandMultiplier();

        List<ProjectileInfo> projectileInfoList = ProjectileInfo.getItemsInfo(mainHand);

        if (projectileInfoList.isEmpty() && offhand.getValue()) {
            ItemStack offHand = mc.player.getOffhandItem();
            handMultiplier = -handMultiplier;
            projectileInfoList = ProjectileInfo.getItemsInfo(offHand);
        }

        if (projectileInfoList.isEmpty()) return;

        showProjectileTrajectory(matrices, projectileInfoList, handMultiplier);
    }

    private void showProjectileTrajectory(PoseStack matrices, List<ProjectileInfo> projectileInfoList, int handMultiplier) {
        float tickProgress = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 eye = mc.player.getEyePosition(tickProgress);

        for (ProjectileInfo projectileInfo : projectileInfoList) {
            Vec3 startPos = projectileInfo.position() == null ? mc.player.getEyePosition() : projectileInfo.position();
            Vec3 handToEyeDelta = calculateHandToEyeDelta(projectileInfo.offset(), startPos, eye, handMultiplier, tickProgress);
            PreviewImpact previewImpact = calculateTrajectory(startPos, projectileInfo);

            renderTargetEffects(matrices, previewImpact);
            renderTrajectory(matrices, previewImpact.points, handToEyeDelta, previewImpact.hit);
        }
    }

    private void renderTargetEffects(PoseStack matrices, PreviewImpact previewImpact) {
        if (previewImpact.impact != null && previewImpact.impact.getType() == HitResult.Type.BLOCK && previewImpact.impact instanceof BlockHitResult bhr) {
            BlockPos impactPos = bhr.getBlockPos();
            if (fillingTargets.is(TrajectoryTargets.BLOCKS) || fillingTargets.is(TrajectoryTargets.BOTH)) {
                RenderUtil.drawBlockFilled(matrices, impactPos, fillingColor.getValue(), false);
            }
            if (outlineTargets.is(TrajectoryTargets.BLOCKS) || outlineTargets.is(TrajectoryTargets.BOTH)) {
                RenderUtil.drawBlockOutline(matrices, impactPos, outlineColor.getValue(), outlineWidth.getFloatValue(), false);
            }
        } else if (previewImpact.entity != null) {
            AABB entityBoundingBox = previewImpact.entity.getBoundingBox().inflate(previewImpact.entity.getPickRadius());
            if (fillingTargets.is(TrajectoryTargets.ENTITIES) || fillingTargets.is(TrajectoryTargets.BOTH)) {
                RenderUtil.drawFilledBox(matrices, entityBoundingBox, fillingColor.getValue(), false);
            }
            if (outlineTargets.is(TrajectoryTargets.ENTITIES) || outlineTargets.is(TrajectoryTargets.BOTH)) {
                RenderUtil.drawOutlineBox(matrices, entityBoundingBox, outlineColor.getValue(), outlineWidth.getFloatValue(), false);
            }
        }
    }

    private Vec3 calculateHandToEyeDelta(Vec3 offset, Vec3 startPos, Vec3 eye, int handMultiplier, float tickProgress) {
        if (mc.gameRenderer.getMainCamera().isDetached()) {
            offset = offset.scale(0);
        }

        float yaw = (float) Math.toRadians(-mc.player.getYRot(tickProgress));
        float pitch = (float) Math.toRadians(-mc.player.getXRot(tickProgress));

        Vec3 forward = mc.player.getViewVector(tickProgress);
        Vec3 up = new Vec3(-Math.sin(pitch) * Math.sin(yaw), Math.cos(pitch), -Math.sin(pitch) * Math.cos(yaw)).normalize();
        Vec3 right = forward.cross(up).normalize();

        Vec3 offsetDelta = right.scale(handMultiplier * offset.x)
                .add(up.scale(offset.y))
                .add(forward.scale(offset.z));

        return offsetDelta.add(eye.subtract(startPos));
    }

    private void renderTrajectory(PoseStack matrices, List<Vec3> trajectoryPoints, Vec3 handToEyeDelta, boolean hasHit) {
        if (trajectoryPoints.isEmpty()) return;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();

        matrices.pushPose();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        renderTrajectorySegments(matrices, trajectoryPoints, handToEyeDelta);

        if (hasHit && impactPoint.getValue()) {
            renderImpactPoint(matrices, trajectoryPoints.getLast());
        }

        matrices.popPose();
    }

    private void renderTrajectorySegments(PoseStack matrices, List<Vec3> points, Vec3 handToEyeDelta) {
        int pointCount = points.size();

        for (int i = 0; i < pointCount - 1; i++) {
            double lerpFactor = (pointCount - i) / (double) pointCount;
            double nextLerpFactor = (pointCount - (i + 1)) / (double) pointCount;

            Vec3 lerpedDelta = handToEyeDelta.scale(lerpFactor);
            Vec3 nextLerpedDelta = handToEyeDelta.scale(nextLerpFactor);

            Vec3 start = points.get(i).add(lerpedDelta);
            Vec3 end = points.get(i + 1).add(nextLerpedDelta);
            Vec3 direction = end.subtract(start);

            if (trajStyle.is(TrajectoryStyle.DASHED)) {
                direction = direction.scale(0.5);
            } else if (trajStyle.is(TrajectoryStyle.DOTTED)) {
                direction = direction.scale(0.15);
            }

            Vector3f startVector = new Vector3f((float) start.x, (float) start.y, (float) start.z);
            RenderUtil.drawVector(matrices, startVector, direction, trajColor.getValue(), trajWidth.getFloatValue());
        }
    }

    private void renderImpactPoint(PoseStack matrices, Vec3 impactPos) {
        double radius = 0.1;
        double diameter = 2 * radius;

        Vector3f xStart = new Vector3f((float) (impactPos.x - radius), (float) impactPos.y, (float) impactPos.z);
        RenderUtil.drawVector(matrices, xStart, new Vec3(diameter, 0, 0), trajColor.getValue(), trajWidth.getFloatValue());

        Vector3f yStart = new Vector3f((float) impactPos.x, (float) (impactPos.y - radius), (float) impactPos.z);
        RenderUtil.drawVector(matrices, yStart, new Vec3(0, diameter, 0), trajColor.getValue(), trajWidth.getFloatValue());

        Vector3f zStart = new Vector3f((float) impactPos.x, (float) impactPos.y, (float) (impactPos.z - radius));
        RenderUtil.drawVector(matrices, zStart, new Vec3(0, 0, diameter), trajColor.getValue(), trajWidth.getFloatValue());
    }

    private PreviewImpact calculateTrajectory(Vec3 startPos, ProjectileInfo projectileInfo) {
        Vec3 currentPos = startPos;
        Vec3 prevPos = startPos;
        Vec3 playerVel = mc.player.onGround() ? new Vec3(mc.player.getDeltaMovement().x, 0,  mc.player.getDeltaMovement().z) : mc.player.getDeltaMovement();
        Vec3 velocity = projectileInfo.initialVelocity().add(playerVel);

        List<Vec3> trajectoryPoints = new ArrayList<>();

        double drag = projectileInfo.drag();
        double gravity = projectileInfo.gravity();

        for (int i = 0; i < 200; i++) {
            trajectoryPoints.add(currentPos);

            for (int order : projectileInfo.order()) {
                switch (order) {
                    case 0 -> currentPos = currentPos.add(velocity);
                    case 1 -> velocity = velocity.scale(drag);
                    case 2 -> velocity = velocity.subtract(0, gravity, 0);
                }
            }

            AABB trajectorySegment = new AABB(prevPos, currentPos).inflate(1);

            Optional<Entity> hitEntity = findHitEntity(prevPos, currentPos, trajectorySegment);

            HitResult blockHit = performRaycast(prevPos, currentPos);

            if (blockHit.getType() == HitResult.Type.MISS) {
                boolean inWater = isInWater(prevPos, currentPos);
                drag = inWater ? projectileInfo.waterDrag() : projectileInfo.drag();
                gravity = inWater ? projectileInfo.underwaterGravity() : projectileInfo.gravity();
            }

            if (handleImpact(blockHit, hitEntity, prevPos, currentPos, trajectoryPoints)) {
                return new PreviewImpact(currentPos, blockHit, hitEntity.orElse(null), true, trajectoryPoints);
            }

            if (currentPos.y < mc.level.getMinY() - 20) {
                break;
            }

            prevPos = currentPos;
        }

        return new PreviewImpact(currentPos, null, null, false, trajectoryPoints);
    }

    private Optional<Entity> findHitEntity(Vec3 start, Vec3 end, AABB searchBox) {
        List<Entity> entities = mc.level.getEntitiesOfClass(Entity.class, searchBox, this::isValidTarget);

        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> raycastHit = entityBox.clip(start, end);

            if (raycastHit.isPresent()) {
                double distance = start.distanceToSqr(raycastHit.get());
                if (distance < closestDistance) {
                    closest = entity;
                    closestDistance = distance;
                }
            }
        }

        return closest != null ? Optional.of(closest) : Optional.empty();
    }

    private boolean isValidTarget(Entity entity) {
        return !entity.isSpectator()
                && entity.isAlive()
                && !(entity instanceof Projectile)
                && !(entity instanceof ItemEntity)
                && !(entity instanceof ExperienceOrb)
                && !(entity instanceof EnderDragon)
                && !(entity instanceof LocalPlayer)
                && !(entity instanceof AreaEffectCloud);
    }

    private HitResult performRaycast(Vec3 start, Vec3 end) {
        return mc.level.clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        mc.player
                )
        );
    }

    private boolean isInWater(Vec3 start, Vec3 end) {
        HitResult waterHit = mc.level.clip(
                new ClipContext(start, end, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.WATER, mc.player)
        );
        return waterHit.getType() != HitResult.Type.MISS;
    }

    private boolean handleImpact(HitResult blockHit, Optional<Entity> hitEntity, Vec3 prevPos,
                                 Vec3 currentPos, List<Vec3> trajectoryPoints) {
        double blockDistance = blockHit.getType() != HitResult.Type.MISS ?
                prevPos.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;

        double entityDistance = hitEntity.map(entity -> prevPos.distanceToSqr(findEntityHitPos(prevPos, currentPos, entity))).orElse(Double.MAX_VALUE);

        if (blockDistance < entityDistance && blockHit.getType() != HitResult.Type.MISS) {
            trajectoryPoints.add(blockHit.getLocation());
            return true;
        } else if (hitEntity.isPresent()) {
            Vec3 entityHitPos = findEntityHitPos(prevPos, currentPos, hitEntity.get());
            trajectoryPoints.add(entityHitPos);
            return true;
        }

        return false;
    }

    private Vec3 findEntityHitPos(Vec3 start, Vec3 end, Entity entity) {
        AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());
        return entityBox.clip(start, end).orElse(end);
    }

    private int getHandMultiplier() {
        return mc.options.mainHand().get() == HumanoidArm.RIGHT ? 1 : -1;
    }


    public record PreviewImpact(
            Vec3 pos,
            HitResult impact,
            Entity entity,
            boolean hit,
            List<Vec3> points
    ) {}

    public enum TrajectoryStyle {
        SOLID("continua"),
        DASHED("discontinua"),
        DOTTED("puntos");

        private final String name;
        TrajectoryStyle(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    public enum TrajectoryTargets {
        BOTH("ambos"),
        ENTITIES("entidades"),
        BLOCKS("bloques"),
        NONE("ninguna");

        private final String name;
        TrajectoryTargets(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}