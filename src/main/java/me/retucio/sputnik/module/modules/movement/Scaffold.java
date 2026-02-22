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

import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.InventoryUtil;
import me.retucio.sputnik.util.Lists;
import me.retucio.sputnik.util.NetworkUtil;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Scaffold extends Module {

    private final SettingGroup sgBlocks = addSg(new SettingGroup("bloques", false));
    private final SettingGroup sgBridge = addSg(new SettingGroup("puente", true));
    private final SettingGroup sgRender = addSg(new SettingGroup("render", false));

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
    ));


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
    ));

    private final BooleanSetting towerWhileMoving = sgBridge.add(new BooleanSetting(
            "torre moviéndose",
            "te permite hacer una torre mientras te mueves",
            false
    ));


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
    ));

    private final NumberSetting lineWidth = sgRender.add(new NumberSetting(
            "grosor de línea",
            "grosor de línea del contorno",
            1.5,
            0.1,
            10,
            0.1
    ));

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
    ));


    private final BlockPos.Mutable bp = new BlockPos.Mutable();
    private Integer mjY = null;


    public Scaffold() {
        super("andamios", "pone bloques bajo tus pies de manera automática", Category.MOVEMENT);

        extend.onUpdate(extendDistance::setVisible);

        tower.onUpdate(v -> {
            towerSpeed.setVisible(v);
            towerWhileMoving.setVisible(v);
        });

        outlines.onUpdate(v -> {
            outlineColor.setVisible(v);
            lineWidth.setVisible(v);
        });

        fillings.onUpdate(fillingColor::setVisible);
    }


    @Override
    public void onDisable() {
        mjY = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        Vec3d pos = mc.player.getEntityPos();

        // extender puente si se está moviendo hacia adelante
        if (extend.getValue() && isMoving() && mc.player.forwardSpeed > 0) {
            Vec3d dir = Vec3d.fromPolar(0, mc.player.getYaw()).normalize().multiply(extendDistance.getValue());
            pos = pos.add(dir.x, 0, dir.z);
        }

        // seleccionar nivel Y
        double y;
        if (mjBridge.getValue() && mjY != null && isMoving()) {
            y = mjY;
        } else {
            y = pos.y - 0.5;
        }

        // descender con la tecla de agacharse
        if (sneakForDown.getValue() && mc.options.sneakKey.isPressed()) {
            if (mc.world.isAir(bp.withY(bp.getY() - 1))) {
                y -= 1;
            }
        }

        // colocar bloque
        bp.set(pos.x, y, pos.z);
        place(bp);

        // colocar bloque encima de la cabeza
        if (headhitter.getValue() && mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed()) {
            place(mc.player.getBlockPos().up(2));
        }

        // poner bloques a los pies del jugador mientras asciende para hacer la torre
        if (tower.getValue()
                && mc.options.jumpKey.isPressed()
                && !mc.options.sneakKey.isPressed()
                && !mc.player.getInventory().getSelectedStack().isEmpty()
                && !mc.world.isAir(mc.player.getBlockPos().down())) {
            Vec3d velocity = mc.player.getVelocity();
            Box playerBox = mc.player.getBoundingBox();

            if (Streams.stream(mc.world.getBlockCollisions(mc.player, playerBox.offset(0, 1, 0))).toList().isEmpty()) {
                if (towerWhileMoving.getValue() || !isMoving()) {
                    velocity = new Vec3d(velocity.x, towerSpeed.getValue(), velocity.z);
                }
                mc.player.setVelocity(velocity);
            } else {
                mc.player.setVelocity(velocity.x, Math.ceil(mc.player.getY()) - mc.player.getY(), velocity.z);
                mc.player.setOnGround(true);
            }
        }
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (outlines.getValue()) RenderUtil.drawBlockOutline(event.getMatrices(), bp, outlineColor.getValue(), lineWidth.getFloatValue(), false);
        if (fillings.getValue()) RenderUtil.drawBlockFilled(event.getMatrices(), bp, fillingColor.getValue(), false);
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
                if (PlayerInventory.isValidHotbarIndex(s)) {
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
        NetworkUtil.placeBlock(
                pos, Hand.MAIN_HAND,
                mc.player.getInventory().getSelectedSlot(),
                rotate.getValue(),
                false,
                true,
                true
        );
    }

    // método helper para ver si un bloque cumple con el filtro
    private boolean isValidBlock(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) return false;
        Block block = ((BlockItem) stack.getItem()).getBlock();

        boolean contains = blocks.isEnabled(block);
        if (blocksFilter.is(ListMode.BLACKLIST) && contains)  return false;
        if (blocksFilter.is(ListMode.WHITELIST) && !contains) return false;

        if (!Block.isShapeFullCube(block.getDefaultState().getCollisionShape(mc.world, BlockPos.ORIGIN))) {
            return false;
        }

        if (block instanceof FallingBlock) {
            return !FallingBlock.canFallThrough(mc.world.getBlockState(mc.player.getBlockPos().down()));
        }

        return true;
    }

    private boolean isMoving() {
        return mc.player.forwardSpeed  != 0
            || mc.player.sidewaysSpeed != 0;
    }

    public enum ListMode {
        WHITELIST("lista blanca (incluir)"),
        BLACKLIST("lista negra (excluir");

        private final String name;
        ListMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
