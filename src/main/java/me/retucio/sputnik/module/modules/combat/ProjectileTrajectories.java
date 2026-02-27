package me.retucio.sputnik.module.modules.combat;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.*;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.misc.ProjectileInfo;
import me.retucio.sputnik.util.render.RenderUtil;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

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

    private void renderProjectileTrajectory(MatrixStack matrices) {
        ItemStack mainHand = mc.player.getMainHandStack();
        int handMultiplier = getHandMultiplier();

        List<ProjectileInfo> projectileInfoList = ProjectileInfo.getItemsInfo(mainHand);

        if (projectileInfoList.isEmpty() && offhand.getValue()) {
            ItemStack offHand = mc.player.getOffHandStack();
            handMultiplier = -handMultiplier;
            projectileInfoList = ProjectileInfo.getItemsInfo(offHand);
        }

        if (projectileInfoList.isEmpty()) return;

        showProjectileTrajectory(matrices, projectileInfoList, handMultiplier);
    }

    private void showProjectileTrajectory(MatrixStack matrices, List<ProjectileInfo> projectileInfoList, int handMultiplier) {
        float tickProgress = mc.getRenderTickCounter().getTickProgress(false);
        Vec3d eye = mc.player.getCameraPosVec(tickProgress);

        for (ProjectileInfo projectileInfo : projectileInfoList) {
            Vec3d startPos = projectileInfo.position() == null ? mc.player.getEyePos() : projectileInfo.position();
            Vec3d handToEyeDelta = calculateHandToEyeDelta(projectileInfo.offset(), startPos, eye, handMultiplier, tickProgress);
            PreviewImpact previewImpact = calculateTrajectory(startPos, projectileInfo);

            renderTargetEffects(matrices, previewImpact);
            renderTrajectory(matrices, previewImpact.points, handToEyeDelta, previewImpact.hit);
        }
    }

    private void renderTargetEffects(MatrixStack matrices, PreviewImpact previewImpact) {
        if (previewImpact.impact != null && previewImpact.impact.getType() == HitResult.Type.BLOCK && previewImpact.impact instanceof BlockHitResult bhr) {
            BlockPos impactPos = bhr.getBlockPos();
            if (fillingTargets.is(TrajectoryTargets.BLOCKS) || fillingTargets.is(TrajectoryTargets.BOTH)) {
                RenderUtil.drawBlockFilled(matrices, impactPos, fillingColor.getValue(), false);
            }
            if (outlineTargets.is(TrajectoryTargets.BLOCKS) || outlineTargets.is(TrajectoryTargets.BOTH)) {
                RenderUtil.drawBlockOutline(matrices, impactPos, outlineColor.getValue(), outlineWidth.getFloatValue(), false);
            }
        } else if (previewImpact.entity != null) {
            Box entityBoundingBox = previewImpact.entity.getBoundingBox().expand(previewImpact.entity.getTargetingMargin());
            if (fillingTargets.is(TrajectoryTargets.ENTITIES) || fillingTargets.is(TrajectoryTargets.BOTH)) {
                RenderUtil.drawFilledBox(matrices, entityBoundingBox, fillingColor.getValue(), false);
            }
            if (outlineTargets.is(TrajectoryTargets.ENTITIES) || outlineTargets.is(TrajectoryTargets.BOTH)) {
                RenderUtil.drawOutlineBox(matrices, entityBoundingBox, outlineColor.getValue(), outlineWidth.getFloatValue(), false);
            }
        }
    }

    private Vec3d calculateHandToEyeDelta(Vec3d offset, Vec3d startPos, Vec3d eye, int handMultiplier, float tickProgress) {
        if (mc.gameRenderer.getCamera().isThirdPerson()) {
            offset = offset.multiply(0);
        }

        float yaw = (float) Math.toRadians(-mc.player.getYaw(tickProgress));
        float pitch = (float) Math.toRadians(-mc.player.getPitch(tickProgress));

        Vec3d forward = mc.player.getRotationVec(tickProgress);
        Vec3d up = new Vec3d(-Math.sin(pitch) * Math.sin(yaw), Math.cos(pitch), -Math.sin(pitch) * Math.cos(yaw)).normalize();
        Vec3d right = forward.crossProduct(up).normalize();

        Vec3d offsetDelta = right.multiply(handMultiplier * offset.x)
                .add(up.multiply(offset.y))
                .add(forward.multiply(offset.z));

        return offsetDelta.add(eye.subtract(startPos));
    }

    private void renderTrajectory(MatrixStack matrices, List<Vec3d> trajectoryPoints, Vec3d handToEyeDelta, boolean hasHit) {
        if (trajectoryPoints.isEmpty()) return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        renderTrajectorySegments(matrices, trajectoryPoints, handToEyeDelta);

        if (hasHit && impactPoint.getValue()) {
            renderImpactPoint(matrices, trajectoryPoints.getLast());
        }

        matrices.pop();
    }

    private void renderTrajectorySegments(MatrixStack matrices, List<Vec3d> points, Vec3d handToEyeDelta) {
        int pointCount = points.size();

        for (int i = 0; i < pointCount - 1; i++) {
            double lerpFactor = (pointCount - i) / (double) pointCount;
            double nextLerpFactor = (pointCount - (i + 1)) / (double) pointCount;

            Vec3d lerpedDelta = handToEyeDelta.multiply(lerpFactor);
            Vec3d nextLerpedDelta = handToEyeDelta.multiply(nextLerpFactor);

            Vec3d start = points.get(i).add(lerpedDelta);
            Vec3d end = points.get(i + 1).add(nextLerpedDelta);
            Vec3d direction = end.subtract(start);

            if (trajStyle.is(TrajectoryStyle.DASHED)) {
                direction = direction.multiply(0.5);
            } else if (trajStyle.is(TrajectoryStyle.DOTTED)) {
                direction = direction.multiply(0.15);
            }

            Vector3f startVector = new Vector3f((float) start.x, (float) start.y, (float) start.z);
            RenderUtil.drawVector(matrices, startVector, direction, trajColor.getValue(), trajWidth.getFloatValue());
        }
    }

    private void renderImpactPoint(MatrixStack matrices, Vec3d impactPos) {
        double radius = 0.1;
        double diameter = 2 * radius;

        Vector3f xStart = new Vector3f((float) (impactPos.x - radius), (float) impactPos.y, (float) impactPos.z);
        RenderUtil.drawVector(matrices, xStart, new Vec3d(diameter, 0, 0), trajColor.getValue(), trajWidth.getFloatValue());

        Vector3f yStart = new Vector3f((float) impactPos.x, (float) (impactPos.y - radius), (float) impactPos.z);
        RenderUtil.drawVector(matrices, yStart, new Vec3d(0, diameter, 0), trajColor.getValue(), trajWidth.getFloatValue());

        Vector3f zStart = new Vector3f((float) impactPos.x, (float) impactPos.y, (float) (impactPos.z - radius));
        RenderUtil.drawVector(matrices, zStart, new Vec3d(0, 0, diameter), trajColor.getValue(), trajWidth.getFloatValue());
    }

    private PreviewImpact calculateTrajectory(Vec3d startPos, ProjectileInfo projectileInfo) {
        Vec3d currentPos = startPos;
        Vec3d prevPos = startPos;
        Vec3d playerVel = mc.player.isOnGround() ? new Vec3d(mc.player.getVelocity().x, 0,  mc.player.getVelocity().z) : mc.player.getVelocity();
        Vec3d velocity = projectileInfo.initialVelocity().add(playerVel);

        List<Vec3d> trajectoryPoints = new ArrayList<>();

        double drag = projectileInfo.drag();
        double gravity = projectileInfo.gravity();

        for (int i = 0; i < 200; i++) {
            trajectoryPoints.add(currentPos);

            for (int order : projectileInfo.order()) {
                switch (order) {
                    case 0 -> currentPos = currentPos.add(velocity);
                    case 1 -> velocity = velocity.multiply(drag);
                    case 2 -> velocity = velocity.subtract(0, gravity, 0);
                }
            }

            Box trajectorySegment = new Box(prevPos, currentPos).expand(1);

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

            if (currentPos.y < mc.world.getBottomY() - 20) {
                break;
            }

            prevPos = currentPos;
        }

        return new PreviewImpact(currentPos, null, null, false, trajectoryPoints);
    }

    private Optional<Entity> findHitEntity(Vec3d start, Vec3d end, Box searchBox) {
        List<Entity> entities = mc.world.getEntitiesByClass(Entity.class, searchBox, this::isValidTarget);

        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
            Optional<Vec3d> raycastHit = entityBox.raycast(start, end);

            if (raycastHit.isPresent()) {
                double distance = start.squaredDistanceTo(raycastHit.get());
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
                && !(entity instanceof ProjectileEntity)
                && !(entity instanceof ItemEntity)
                && !(entity instanceof ExperienceOrbEntity)
                && !(entity instanceof EnderDragonEntity)
                && !(entity instanceof ClientPlayerEntity)
                && !(entity instanceof AreaEffectCloudEntity);
    }

    private HitResult performRaycast(Vec3d start, Vec3d end) {
        return mc.world.raycast(
                new RaycastContext(
                        start,
                        end,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        mc.player
                )
        );
    }

    private boolean isInWater(Vec3d start, Vec3d end) {
        HitResult waterHit = mc.world.raycast(
                new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.WATER, mc.player)
        );
        return waterHit.getType() != HitResult.Type.MISS;
    }

    private boolean handleImpact(HitResult blockHit, Optional<Entity> hitEntity, Vec3d prevPos,
                                 Vec3d currentPos, List<Vec3d> trajectoryPoints) {
        double blockDistance = blockHit.getType() != HitResult.Type.MISS ?
                prevPos.squaredDistanceTo(blockHit.getPos()) : Double.MAX_VALUE;

        double entityDistance = hitEntity.map(entity -> prevPos.squaredDistanceTo(findEntityHitPos(prevPos, currentPos, entity))).orElse(Double.MAX_VALUE);

        if (blockDistance < entityDistance && blockHit.getType() != HitResult.Type.MISS) {
            trajectoryPoints.add(blockHit.getPos());
            return true;
        } else if (hitEntity.isPresent()) {
            Vec3d entityHitPos = findEntityHitPos(prevPos, currentPos, hitEntity.get());
            trajectoryPoints.add(entityHitPos);
            return true;
        }

        return false;
    }

    private Vec3d findEntityHitPos(Vec3d start, Vec3d end, Entity entity) {
        Box entityBox = entity.getBoundingBox().expand(entity.getTargetingMargin());
        return entityBox.raycast(start, end).orElse(end);
    }

    private int getHandMultiplier() {
        return mc.options.getMainArm().getValue() == Arm.RIGHT ? 1 : -1;
    }


    public record PreviewImpact(
            Vec3d pos,
            HitResult impact,
            Entity entity,
            boolean hit,
            List<Vec3d> points
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