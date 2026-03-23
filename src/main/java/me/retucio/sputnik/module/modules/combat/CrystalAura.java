package me.retucio.sputnik.module.modules.combat;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.*;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author retucio
 */
public class CrystalAura extends Module {

    private final SettingGroup sgPlace = addSg(new SettingGroup("colocar", false));
    private final SettingGroup sgBreak = addSg(new SettingGroup("reventar", false));
    private final SettingGroup sgRender = addSg(new SettingGroup("renderizado", false));

    private final ListSetting<EntityType<?>> entities = sgGeneral.add(new ListSetting<>(
            "entidades", "entidades a las que atacar",
            Lists.entityList, Lists.allFalseExcept(Lists.entityList, EntityType.PLAYER), Lists.entityNames
    ));

    private final NumberSetting targetRange = sgGeneral.add(new NumberSetting(
            "rango",
            "rango de detección de blancos",
            5, 0, 12, 0.1
    ));


    // ajustes al colocar los cristales

    private final BooleanSetting placeCrystals = sgPlace.add(new BooleanSetting(
            "colocar", "colocar los cristales", true
    ));

    private final NumberSetting placeDelay = sgPlace.add(new NumberSetting(
            "delay", "delay entre cristal y cristal",
            0, 0, 20, 1
    )).visibility(placeCrystals::getValue);

    private final NumberSetting placeRange = sgPlace.add(new NumberSetting(
            "rango ", "rango máximo para colocar cristales",
            4.5, 0, 6, 0.1
    )).visibility(placeCrystals::getValue);

    private final BooleanSetting rotateToPlace = sgPlace.add(new BooleanSetting(
            "rotar", "rotar para colocar cristales", false
    )).visibility(placeCrystals::getValue);

    private final BooleanSetting autoSelect = sgPlace.add(new BooleanSetting(
            "autoseleccionar", "seleccionar cristales automáticamente", true
    ));


    // ajustes al reventar los cristales

    private final BooleanSetting breakCrystals = sgBreak.add(new BooleanSetting(
            "reventar", "reventar los cristales", true
    ));

    private final NumberSetting breakDelay = sgBreak.add(new NumberSetting(
            "delay ", "delay entre cristal y cristal",
            0, 0, 20, 1
    )).visibility(breakCrystals::getValue);

    private final BooleanSetting rotateToBreak = sgBreak.add(new BooleanSetting(
            "rotar", "rotar para reventar cristales", false
    )).visibility(breakCrystals::getValue);


    // ajustes de renderizado

    private final BooleanSetting outlines = sgRender.add(new BooleanSetting(
            "contorno", "renderizar contorno del bloque donde se colocan los cristales", true
    ));

    private final ColorSetting outlineColor = sgRender.add(new ColorSetting(
            "color del contorno", "color del contorno",
            Colors.mainColor, false
    )).visibility(outlines::getValue);

    private final NumberSetting outlineWidth = sgRender.add(new NumberSetting(
            "grosor del contorno", "grosor del contorno",
            2, 0, 15, 0.1
    )).visibility(outlines::getValue);

    private final BooleanSetting fillings = sgRender.add(new BooleanSetting(
            "relleno", "renderizar relleno del bloque donde se colocan los cristales", false
    ));

    private final ColorSetting fillingColor = sgRender.add(new ColorSetting(
            "color del relleno", "color del relleno",
            Colors.withAlpha(Colors.mainColor, 70), false
    )).visibility(fillings::getValue);


    private final List<LivingEntity> targets = new ArrayList<>();
    private BlockPos bp = null;

    private int placeTimer = placeDelay.getIntValue();
    private int breakTimer = breakDelay.getIntValue();


    public CrystalAura() {
        super("aura de cristales",
                "coloca y revienta cristales del end por ti", Category.COMBAT);
    }


    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null || mc.world == null || mc.getCameraEntity() == null) return;

        if (placeTimer > 0) placeTimer--;
        if (breakTimer > 0) breakTimer--;

        findTargets();

        if (!targets.isEmpty()) {
            if (placeCrystals.getValue()) doPlace();
            if (breakCrystals.getValue()) doBreak();
        } else {
            bp = null;
        }
    }

    // colocar cristal
    private void doPlace() {
        if (placeTimer > 0) return;

        if (!canPlaceCrystal()) {
            bp = null;
            return;
        }

        // elegir como blanco primario la entidad más cercana
        LivingEntity primaryTarget = null;
        double closestTargetDist = Double.MAX_VALUE;
        for (LivingEntity target : targets) {
            double dist = mc.player.squaredDistanceTo(target);
            if (dist < closestTargetDist) {
                closestTargetDist = dist;
                primaryTarget = target;
            }
        }
        if (primaryTarget == null) {
            bp = null;
            return;
        }

        final Vec3d targetPos = primaryTarget.getEntityPos();
        final BlockPos.Mutable[] bestPos = new BlockPos.Mutable[1];
        final double[] bestDist = { Double.MAX_VALUE };

        BlockIterator.register((int) Math.ceil(placeRange.getValue()), (int) Math.ceil(placeRange.getValue()), (pos, state) -> {
            if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK)) return;
            if (!mc.world.isAir(pos.add(0, 1, 0))) return;

            // cálculo de mejor distancia
            double dist = targetPos.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (dist < bestDist[0]) {
                bestDist[0] = dist;
                if (bestPos[0] == null) bestPos[0] = new BlockPos.Mutable();
                bestPos[0].set(pos);
            }
        });

        BlockIterator.after(() -> {
            if (bestPos[0] != null) {
                BlockHitResult result = getBlockInfo(bestPos[0]);
                placeCrystal(result);
                bp = bestPos[0].toImmutable();  // guardar para el renderizado
            } else {
                bp = null;
            }
        });

        placeTimer = placeDelay.getIntValue();
    }

    private void doBreak() {
        if (breakTimer > 0) return;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity) || entity.distanceTo(mc.player) > mc.player.getEntityInteractionRange()) continue;
            if (rotateToBreak.getValue()) EntityUtil.lookAt(entity.getEyePos());
            mc.interactionManager.attackEntity(mc.player, entity);
        }
        breakTimer = breakDelay.getIntValue();
    }

    private void placeCrystal(BlockHitResult result) {
        if (autoSelect.getValue()) {
            int slot = InventoryUtil.findSlot(stack -> stack.isOf(Items.END_CRYSTAL));
            if (slot != InventoryUtil.OFFHAND_SLOT) {
                if (!PlayerInventory.isValidHotbarIndex(slot)) {
                    InventoryUtil.swapWithHotbar(slot, mc.player.getInventory().getSelectedSlot());
                } else {
                    mc.player.getInventory().setSelectedSlot(slot);
                }
            }
        }

        Hand hand;
        if (mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
            hand = Hand.MAIN_HAND;
        } else if (mc.player.getOffHandStack().isOf(Items.END_CRYSTAL)) {
            hand = Hand.OFF_HAND;
        } else {
            return;
        }

        if (rotateToPlace.getValue()) EntityUtil.lookAt(result.getBlockPos().toCenterPos());
        mc.interactionManager.sendSequencedPacket(mc.world, (sequence) -> new PlayerInteractBlockC2SPacket(hand, result, sequence));
    }

    // encontrar blancos
    private void findTargets() {
        targets.clear();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity entity)) continue;
            if (!entity.isAlive() || entity.isInCreativeMode() || entity.equals(mc.player)) continue;
            if (!entities.isEnabled(entity.getType())) continue;
            if (entity.squaredDistanceTo(mc.player) > targetRange.getValue() * targetRange.getValue()) continue;
            targets.add(entity);
        }
    }

    private BlockHitResult getBlockInfo(BlockPos pos) {
        Vec3d start = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        Vec3d end;

        for (Direction side : Direction.values()) {
            end = new Vec3d(
                    pos.getX() + 0.5 + side.getVector().getX() * 0.5,
                    pos.getY() + 0.5 + side.getVector().getY() * 0.5,
                    pos.getZ() + 0.5 + side.getVector().getZ() * 0.5
            );

            BlockHitResult result = mc.world.raycast(new RaycastContext(
                    start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player
            ));

            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return result;
            }
        }

        Direction side = pos.getY() > start.y ? Direction.DOWN : Direction.UP;
        return new BlockHitResult(start, side, pos, false);
    }

    private boolean canPlaceCrystal() {
        if (autoSelect.getValue()) {
            return InventoryUtil.findSlot(stack -> stack.isOf(Items.END_CRYSTAL)) != -1;
        } else {
            return InventoryUtil.hasInHotbar(Items.END_CRYSTAL);
        }
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (bp != null) {
            if (outlines.getValue()) {
                RenderUtil.drawBlockOutline(event.getMatrices(), bp, outlineColor.getValue(), outlineWidth.getFloatValue(), false);
            }
            if (fillings.getValue()) {
                RenderUtil.drawBlockFilled(event.getMatrices(), bp, fillingColor.getValue(), false);
            }
        }
    }
}