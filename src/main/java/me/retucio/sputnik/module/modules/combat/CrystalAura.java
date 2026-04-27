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
import me.retucio.sputnik.util.misc.BlockIterator;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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

    private final BooleanSetting placeObsidian = sgPlace.add(new BooleanSetting(
            "colocar obsidiana", "colocar obsidiana si es necesario", true
    )).visibility(placeCrystals::getValue);

    private final BooleanSetting autoSelect = sgPlace.add(new BooleanSetting(
            "autoseleccionar", "seleccionar cristales automáticamente", true
    )).visibility(placeCrystals::getValue);


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
        if (mc.player == null || mc.gameMode == null || mc.level == null || mc.getCameraEntity() == null) return;

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

    @SuppressWarnings("deprecation")
    // colocar cristal (u obsidiana si es necesario)
    private void doPlace() {
        if (placeTimer > 0) return;

        if (!hasCrystal()) {
            bp = null;
            return;
        }

        // elegir como blanco primario la entidad más cercana
        LivingEntity primaryTarget = null;
        double closestTargetDist = Double.MAX_VALUE;
        for (LivingEntity target : targets) {
            double dist = mc.player.distanceToSqr(target);
            if (dist < closestTargetDist) {
                closestTargetDist = dist;
                primaryTarget = target;
            }
        }
        if (primaryTarget == null) {
            bp = null;
            return;
        }

        final Vec3 targetPos = primaryTarget.position();

        final BlockPos.MutableBlockPos[] bestExistingPos = new BlockPos.MutableBlockPos[1];
        final double[] bestExistingDist = { Double.MAX_VALUE };
        final BlockPos.MutableBlockPos[] bestObbyPlacePos = new BlockPos.MutableBlockPos[1];
        final double[] bestObbyPlaceDist = { Double.MAX_VALUE };

        BlockIterator.register((int) Math.ceil(placeRange.getValue()), (int) Math.ceil(placeRange.getValue()), (pos, state) -> {
            if ((state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK)) && mc.level.isEmptyBlock(pos.above())) {
                double dist = targetPos.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (dist < bestExistingDist[0]) {
                    bestExistingDist[0] = dist;
                    if (bestExistingPos[0] == null) bestExistingPos[0] = new BlockPos.MutableBlockPos();
                    bestExistingPos[0].set(pos);
                }
            }
            //                                                                                                      asegurarse de que no está dentro del bloque
            if (mc.level.isEmptyBlock(pos) && mc.level.getBlockState(pos.below()).isSolid() && mc.level.isEmptyBlock(pos.above()) && targetPos.distanceTo(pos.getCenter()) > 0.6) {
                double dist = targetPos.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (dist < bestObbyPlaceDist[0]) {
                    bestObbyPlaceDist[0] = dist;
                    if (bestObbyPlacePos[0] == null) bestObbyPlacePos[0] = new BlockPos.MutableBlockPos();
                    bestObbyPlacePos[0].set(pos);
                }
            }
        });

        BlockIterator.after(() -> {
            if (bestExistingPos[0] != null) {
                BlockHitResult result = getBlockInfo(bestExistingPos[0]);
                placeCrystal(result);
                bp = bestExistingPos[0].immutable();
            } else if (hasObby() && bestObbyPlacePos[0] != null) {
                placeObby(bestObbyPlacePos[0]);
                bp = bestObbyPlacePos[0].immutable();  // mostrar el bloque donde se pondrá el cristal
            } else {
                bp = null;
            }
        });

        placeTimer = placeDelay.getIntValue();
    }

    private void doBreak() {
        if (breakTimer > 0) return;
        for (Entity entity : mc.level.getEntities().getAll()) {
            if (!(entity instanceof EndCrystal) || entity.distanceTo(mc.player) > mc.player.entityInteractionRange()) continue;
            if (rotateToBreak.getValue()) EntityUtil.lookAt(entity.getEyePosition());
            mc.gameMode.attack(mc.player, entity);
        }
        breakTimer = breakDelay.getIntValue();
    }

    private void placeCrystal(BlockHitResult result) {
        doAutoSelect(Items.END_CRYSTAL);

        InteractionHand hand;
        if (mc.player.getMainHandItem().is(Items.END_CRYSTAL)) {
            hand = InteractionHand.MAIN_HAND;
        } else if (mc.player.getOffhandItem().is(Items.END_CRYSTAL)) {
            hand = InteractionHand.OFF_HAND;
        } else {
            return;
        }

        if (rotateToPlace.getValue()) EntityUtil.lookAt(result.getBlockPos().getCenter());
        mc.gameMode.startPrediction(mc.level, (sequence) -> new ServerboundUseItemOnPacket(hand, result, sequence));
    }

    private void placeObby(BlockPos pos) {
        doAutoSelect(Items.OBSIDIAN);

        InteractionHand hand;
        if (mc.player.getMainHandItem().is(Items.OBSIDIAN)) {
            hand = InteractionHand.MAIN_HAND;
        } else if (mc.player.getOffhandItem().is(Items.OBSIDIAN)) {
            hand = InteractionHand.OFF_HAND;
        } else {
            return;
        }

        BlockPos supportPos = pos.below();
        BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(supportPos), Direction.UP, supportPos, false);

        if (rotateToPlace.getValue()) EntityUtil.lookAt(pos.getCenter());
        mc.gameMode.startPrediction(mc.level, (sequence) -> new ServerboundUseItemOnPacket(hand, result, sequence));
    }

    private void doAutoSelect(Item item) {
        int slot = InventoryUtil.findSlot(stack -> stack.is(item));
        if (slot != InventoryUtil.OFFHAND_SLOT) {
            if (!Inventory.isHotbarSlot(slot)) {
                InventoryUtil.swapWithHotbar(slot, mc.player.getInventory().getSelectedSlot());
            } else {
                mc.player.getInventory().setSelectedSlot(slot);
            }
        }
    }

    private boolean hasObby() {
        if (!placeObsidian.getValue()) return false;
        if (autoSelect.getValue()) {
            return InventoryUtil.findSlot(stack -> stack.is(Items.OBSIDIAN)) != -1;
        } else {
            return InventoryUtil.hasInHotbar(Items.OBSIDIAN);
        }
    }

    private void findTargets() {
        targets.clear();
        for (Entity e : mc.level.getEntities().getAll()) {
            if (!(e instanceof LivingEntity entity)) continue;
            if (!entity.isAlive() || entity.hasInfiniteMaterials() || entity.equals(mc.player)) continue;
            if (!entities.isEnabled(entity.getType())) continue;
            if (entity.distanceToSqr(mc.player) > targetRange.getValue() * targetRange.getValue()) continue;
            targets.add(entity);
        }
    }

    private BlockHitResult getBlockInfo(BlockPos pos) {
        Vec3 start = new Vec3(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        Vec3 end;

        for (Direction side : Direction.values()) {
            end = new Vec3(
                    pos.getX() + 0.5 + side.getUnitVec3().x() * 0.5,
                    pos.getY() + 0.5 + side.getUnitVec3().y() * 0.5,
                    pos.getZ() + 0.5 + side.getUnitVec3().z() * 0.5
            );

            BlockHitResult result = mc.level.clip(new ClipContext(
                    start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player
            ));

            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return result;
            }
        }

        Direction side = pos.getY() > start.y ? Direction.DOWN : Direction.UP;
        return new BlockHitResult(start, side, pos, false);
    }

    private boolean hasCrystal() {
        if (autoSelect.getValue()) {
            return InventoryUtil.findSlot(stack -> stack.is(Items.END_CRYSTAL)) != -1;
        } else {
            return InventoryUtil.hasInHotbar(Items.END_CRYSTAL);
        }
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (bp == null) return;
        if (outlines.getValue()) {
            RenderUtil.drawBlockOutline(event.getMatrices(), bp, outlineColor.getValue(), outlineWidth.getFloatValue(), false);
        }
        if (fillings.getValue()) {
            RenderUtil.drawBlockFilled(event.getMatrices(), bp, fillingColor.getValue(), false);
        }
    }
}