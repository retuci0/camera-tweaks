package me.retucio.sputnik.module.modules.movement;

import com.github.retucio.neutrino.EventListener;
import com.google.common.collect.Streams;
import me.retucio.sputnik.event.input.SneakEvent;
import me.retucio.sputnik.event.interact.PlaceBlockEvent;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.*;

import me.retucio.sputnik.util.*;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Scaffold extends Module {

    private final SettingGroup sgBlocks = addSg(new SettingGroup("bloques", false));
    private final SettingGroup sgBridge = addSg(new SettingGroup("puente", true));
    private final SettingGroup sgRender = addSg(new SettingGroup("renderizado", false));

    // todo: arreglar esta puta mierda
    private final BooleanSetting rotate = sgGeneral.add(new BooleanSetting(
            "rotar",
            "mover la cabeza",
            false
    ));


    /* ajustes de selección de bloques */

    private final ListSetting<Block> blocks = sgBlocks.add(new ListSetting<>(
            "bloques",
            "bloques a aplicar al filtro",
            Lists.blockList,
            Lists.allFalse(Lists.blockList),
            Lists.blockNames
    ));

    private final EnumSetting<ListMode> blocksFilter = sgBlocks.add(new EnumSetting<>(
            "filtro",
            "tipo de filtro a usar",
            ListMode.class, ListMode.BLACKLIST
    ));

    private final BooleanSetting autoSelectItem = sgBlocks.add(new BooleanSetting(
            "autoselect",
            "seleccionar slot más cercano con bloques automáticamente",
            true
    ));


    /* extender puente */

    private final BooleanSetting extend = sgBridge.add(new BooleanSetting(
            "extender",
            "poner bloques en frente al caminar",
            false
    ));

    private final NumberSetting extendDistance = sgBridge.add(new NumberSetting(
            "distancia a extender",
            "cuánto extender",
            2,
            0,
            4,
            0.1
    )).visibility(extend::getValue);


    /* torre */

    private final BooleanSetting tower = sgBridge.add(new BooleanSetting(
            "torre",
            "hacer torres verticales chulas",
            false
    ));

    private final NumberSetting towerSpeed = sgBridge.add(new NumberSetting(
        "velocidad de torre",
            "a qué velocidad hacer la torre",
            0.5,
            0,
            1,
            0.01
    )).visibility(tower::getValue);

    private final BooleanSetting towerWhileMoving = sgBridge.add(new BooleanSetting(
            "torre moviéndose",
            "te permite hacer una torre mientras te mueves",
            false
    )).visibility(tower::getValue);


    /* otros */

    private final BooleanSetting mjBridge = sgBridge.add(new BooleanSetting(
            "mj bridge",
            "hacer que saltar no eleve el nivel y del puente",
            false
    ));

    private final BooleanSetting sneakForDown = sgBridge.add(new BooleanSetting(
            "descender",
            "agacharse para descender",
            true
    ));

    private final BooleanSetting headhitter = sgBridge.add(new BooleanSetting(
            "headhitter",
            "poner bloques sobre tu cabeza",
            false
    ));


    /* ajustes de renderizado */

    private final BooleanSetting outlines = sgRender.add(new BooleanSetting(
            "contorno",
            "renderizar contorno del bloque colocado",
            true
    ));

    private final ColorSetting outlineColor = sgRender.add(new ColorSetting(
            "color del contorno",
            "color a usar al renderizar el contorno",
            Colors.mainColor,
            false
    )).visibility(outlines::getValue);

    private final NumberSetting lineWidth = sgRender.add(new NumberSetting(
            "grosor de línea",
            "grosor de línea del contorno",
            1.5,
            0.1,
            10,
            0.1
    )).visibility(outlines::getValue);

    private final BooleanSetting fillings = sgRender.add(new BooleanSetting(
            "relleno",
            "renderizar relleno del bloque colocado",
            false
    ));

    private final ColorSetting fillingColor = sgRender.add(new ColorSetting(
            "color del relleno",
            "color a uisar al renderizar el relleno",
            Colors.withAlpha(Colors.mainColor, 60),
            false
    )).visibility(fillings::getValue);

    private final BooleanSetting showLastPlaced = sgRender.add(new BooleanSetting(
            "mostrar últimos bloques",
            "mostrar los últimos bloques colocados",
            true
    ));

    private final BooleanSetting fade = sgRender.add(new BooleanSetting(
            "gradiente",
            "gradiente de opacidad de los últimos bloques colocados",
            true
    )).visibility(showLastPlaced::getValue);

    private final NumberSetting lifeTime = sgRender.add(new NumberSetting(
            "esperanza de vida",
            "esperanza de vida de los últimos bloques colocados (renderizado)",
            1,
            0,
            5,
            0.1
    )).visibility(showLastPlaced::getValue);


    private final BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
    private final List<PlacedBlock> lastPlacedBlocks = new ArrayList<>();
    private Integer mjY = null;


    public Scaffold() {
        super("andamios", "pone bloques bajo tus pies de manera automática", Category.MOVEMENT);
    }


    @Override
    public void onDisable() {
        mjY = null;
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        Vec3 pos = mc.player.position();

        // extender puente si se está moviendo hacia adelante
        if (extend.getValue() && isMoving() && mc.player.zza > 0) {
            Vec3 dir = Vec3.directionFromRotation(0, mc.player.getYRot()).normalize().scale(extendDistance.getValue());
            pos = pos.add(dir.x, 0, dir.z);
        }

        lastPlacedBlocks.removeIf(block -> {
            block.age++;
            return block.age >= block.lifeTime;
        });

        // seleccionar nivel Y
        double y;
        if (mjBridge.getValue() && mjY != null && isMoving()) {
            y = mjY;
        } else {
            y = pos.y - 0.5;
        }

        // descender con la tecla de agacharse
        if (sneakForDown.getValue() && mc.options.keyShift.isDown()) {
            if (mc.level.isEmptyBlock(bp.atY(bp.getY() - 1))) {
                y -= 1;
            }
        }

        // colocar bloque
        bp.set(pos.x, y, pos.z);
        place(bp);

        // colocar bloque encima de la cabeza
        if (headhitter.getValue() && mc.options.keyJump.isDown() && !mc.options.keyShift.isDown()) {
            place(mc.player.blockPosition().above(2));
        }

        // poner bloques a los pies del jugador mientras asciende para hacer la torre
        if (tower.getValue()
                && mc.options.keyJump.isDown()
                && !mc.options.keyShift.isDown()
                && mc.player.getInventory().getSelectedItem().getItem() instanceof BlockItem
                && !mc.level.isEmptyBlock(mc.player.blockPosition().below())) {
            Vec3 velocity = mc.player.getDeltaMovement();
            AABB playerBox = mc.player.getBoundingBox();

            if (Streams.stream(mc.level.getBlockCollisions(mc.player, playerBox.move(0, 1, 0))).toList().isEmpty()) {
                if (towerWhileMoving.getValue() || !isMoving()) {
                    velocity = new Vec3(velocity.x, towerSpeed.getValue(), velocity.z);
                }
                mc.player.setDeltaMovement(velocity);
            } else {
                mc.player.setDeltaMovement(velocity.x, Math.ceil(mc.player.getY()) - mc.player.getY(), velocity.z);
                mc.player.setOnGround(true);
            }
        }
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (fillings.getValue()) RenderUtil.drawBlockFilled(event.getMatrices(), bp, fillingColor.getValue(), false);
        if (outlines.getValue()) RenderUtil.drawBlockOutline(event.getMatrices(), bp, outlineColor.getValue(), lineWidth.getFloatValue(), false);

        if (!showLastPlaced.getValue()) return;
        for (PlacedBlock block : lastPlacedBlocks) {
            if (bp.toMutable().equals(block.pos.toMutable())) continue;
            if (fillings.getValue()) {
                Color fc = fade.getValue()
                        ? Colors.withAlpha(fillingColor.getValue(), (int) ((1 - ((float) block.age / (float) block.lifeTime)) * fillingColor.getA()))
                        : fillingColor.getValue();
                RenderUtil.drawBlockFilled(event.getMatrices(), block.pos, fc, false);
            }
            if (outlines.getValue()) {
                Color oc = fade.getValue()
                        ? Colors.withAlpha(outlineColor.getValue(), (int) ((1 - ((float) block.age / (float) block.lifeTime)) * outlineColor.getA()))
                        : outlineColor.getValue();
                RenderUtil.drawBlockOutline(event.getMatrices(), block.pos, oc, lineWidth.getFloatValue(), false);
            }
        }
    }

    @EventListener
    private void onPlaceBlock(PlaceBlockEvent event) {
        // guardar último valor Y para el mj bridge
        if (mjBridge.getValue()) {
            mjY = event.getResult().getBlockPos().getY();
        }
    }

    @EventListener
    private void onSneak(SneakEvent event) {
        // no agacharse
        if (sneakForDown.getValue()) {
            event.cancel();
        }
    }

    private void place(BlockPos pos) {
        int slot = -1;
        // seleccionar bloque
        if (autoSelectItem.getValue()) {
            List<Integer> slots = InventoryUtil.findAllSlots(this::isValidBlock);
            for (int s : slots) {
                if (Inventory.isHotbarSlot(s)) {
                    slot = s;
                    break;
                }
            }
            if (slot == -1) return;
        } else {
            slot = mc.player.getInventory().getSelectedSlot();
        }

        if (mc.player.getInventory().getSelectedSlot() != slot) {
            mc.player.getInventory().setSelectedSlot(slot);
        }


        // colocar bloque
        if (NetworkUtil.placeBlock(
                pos, InteractionHand.MAIN_HAND,
                mc.player.getInventory().getSelectedSlot(),
                rotate.getValue(),
                false,
                true,
                true
        )) {
            lastPlacedBlocks.add(new PlacedBlock(pos.immutable(), lifeTime.getIntValue() * 20));
        }
    }

    private static class PlacedBlock {
        BlockPos pos;
        final int lifeTime;
        int age;

        PlacedBlock(BlockPos pos, int lifeTime) {
            this.pos = pos;
            this.lifeTime = lifeTime;
            this.age = 0;
        }
    }

    // método helper para ver si un bloque cumple con el filtro
    private boolean isValidBlock(@NonNull ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) return false;
        Block block = ((BlockItem) stack.getItem()).getBlock();

        boolean contains = blocks.isEnabled(block);
        if (blocksFilter.is(ListMode.BLACKLIST) && contains)  return false;
        if (blocksFilter.is(ListMode.WHITELIST) && !contains) return false;

        if (!Block.isShapeFullBlock(block.defaultBlockState().getCollisionShape(mc.level, BlockPos.ZERO))) {
            return false;
        }

        if (block instanceof FallingBlock) {
            return !FallingBlock.isFree(mc.level.getBlockState(mc.player.blockPosition().below()));
        }

        return true;
    }

    private boolean isMoving() {
        return mc.player.zza  != 0
            || mc.player.xxa != 0;
    }

    public enum ListMode {
        WHITELIST("lista blanca (incluir)"),
        BLACKLIST("lista negra (excluir)");

        private final String name;
        ListMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
