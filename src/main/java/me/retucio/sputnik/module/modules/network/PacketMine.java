package me.retucio.sputnik.module.modules.network;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.interact.AttackBlockEvent;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class PacketMine extends Module {

    private final  SettingGroup sgRender = addSg(new SettingGroup("renderizado", true));

    private final NumberSetting retryTime = sgGeneral.add(new NumberSetting(
            "delay",
            "delay entre reintentos de minado",
            30,
            0,
            1000,
            1
    ));

    private final BooleanSetting cancelOutOfReach = sgGeneral.add(new BooleanSetting(
            "cancelar fuera de alcance",
            "cancelar aquellos paquetes que intenten romper bloques fuera del alcance del jugador, para evitar flaggear el anticheat",
            true
    ));

    private final EnumSetting<ClickAction> clickAction = sgGeneral.add(new EnumSetting<>(
            "acción de bloque seleccionado",
            "acción a llevar a cabo al clicar sobre un bloque ya seleccionado",
            ClickAction.class,
            ClickAction.ADD_PRIORITY
    ));

    private final NumberSetting selectionDelay = sgGeneral.add(new NumberSetting(
            "delay",
            "delay entre selecciones de bloques",
            5,
            0,
            20,
            1
    ));

    // no funciona del todo
    private final BooleanSetting inOrder = sgGeneral.add(new BooleanSetting(
            "en orden",
            "romper los bloques en orden forzosamente",
            false
    ));

    private final BooleanSetting render = sgRender.add(new BooleanSetting(
            "renderizar",
            "renderizar un contorno alrededor de los bloques que están siendo minados",
            true
    ));

    private final ColorSetting color = sgRender.add(new ColorSetting(
            "color",
            "color del contorno renderizado",
            new Color(0, 255, 0, 100),
            false
    ));

    private final NumberSetting lineWidth = sgRender.add(new NumberSetting(
            "grosor",
            "grosor de las líneas del contorno",
            2,
            0.1,
            10,
            0.1
    ));

    private final BooleanSetting dontBreak = sgGeneral.add(new BooleanSetting(
            "no romper",
            "evita romper el pico",
            true
    ));

    public PacketMine() {
        super("selección de minado",
                "selecciona bloques a minar con paquetes",
                Category.NETWORK);

        render.onUpdate(v -> {
            color.setVisible(v);
            lineWidth.setVisible(v);
        });
    }

    private final List<MiningBlock> miningBlocks = new ArrayList<>();
    private int currentOrderIndex = 0;
    private long lastSelectionTime = 0;

    @Override
    public void onDisable() {
        miningBlocks.clear();
        currentOrderIndex = 0;
        lastSelectionTime = 0;
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (miningBlocks.isEmpty()) {
            currentOrderIndex = 0;
            return;
        }

        Iterator<MiningBlock> iterator = miningBlocks.iterator();
        while (iterator.hasNext()) {
            MiningBlock block = iterator.next();
            if (mc.world.getBlockState(block.pos).isAir()) {
                iterator.remove();
                if (currentOrderIndex >= miningBlocks.size()) {
                    currentOrderIndex = 0;
                }
            }
        }

        if (clickAction.is(ClickAction.ADD_PRIORITY)) {
            miningBlocks.sort(Comparator.comparingInt(block -> -block.clicks));
            for (MiningBlock block : miningBlocks) {
                if (processBlock(block)) {
                    break;
                }
            }
        } else {
            if (inOrder.getValue()) {
                if (!miningBlocks.isEmpty()) {
                    int startIndex = currentOrderIndex;
                    boolean processed = false;

                    do {  // primera vez en mi vida que uso un do-while
                        MiningBlock block = miningBlocks.get(currentOrderIndex);
                        if (processBlock(block)) {
                            processed = true;
                            currentOrderIndex = (currentOrderIndex + 1) % miningBlocks.size();
                            break;
                        }
                        if (!miningBlocks.isEmpty()) {
                            currentOrderIndex = (currentOrderIndex + 1) % miningBlocks.size();
                        }
                    } while (currentOrderIndex != startIndex);

                    if (!processed) {
                        currentOrderIndex = 0;
                    }
                }
            } else {
                for (MiningBlock block : miningBlocks) {
                    processBlock(block);
                }
            }
        }
    }

    @EventListener
    private void onAttackBlock(AttackBlockEvent event) {
        event.cancel();

        long now = System.currentTimeMillis();
        int delayTicks = selectionDelay.getIntValue();
        if (delayTicks > 0 && now - lastSelectionTime < delayTicks * 50L) {
            return;
        }
        lastSelectionTime = now;

        MiningBlock existingBlock = getMiningBlock(event.getPos());
        if (existingBlock == null) {
            miningBlocks.add(new MiningBlock(event));
            if (inOrder.getValue()) {
                currentOrderIndex = 0;
            }
        } else {
            if (clickAction.is(ClickAction.ADD_PRIORITY)) {
                existingBlock.clicks += 1;
            } else if (clickAction.is(ClickAction.UNSELECT)) {
                miningBlocks.remove(existingBlock);
                if (currentOrderIndex >= miningBlocks.size()) {
                    currentOrderIndex = 0;
                }
            }
        }
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (!render.getValue()) return;
        for (MiningBlock block : miningBlocks) {
            block.render(event);
        }
    }

    private boolean processBlock(MiningBlock block) {
        // por si las moscas
        if (mc.world.getBlockState(block.pos).isAir()) {
            return false;
        }

        if (!block.mining) {
            return block.mine();
        } else if (block.shouldRetry()) {
            block.startTime = System.currentTimeMillis();
            block.mining = false;
            return block.mine();
        }

        return false;
    }

    private boolean isMining(BlockPos pos) {
        for (MiningBlock block : miningBlocks) {
            if (block.pos.equals(pos)) return true;
        }
        return false;
    }

    private MiningBlock getMiningBlock(BlockPos pos) {
        for (MiningBlock block : miningBlocks) {
            if (block.pos.equals(pos)) return block;
        }
        return null;
    }

    private class MiningBlock {
        int clicks = 1;
        BlockPos pos;
        Direction dir;
        boolean mining = false;
        long startTime;

        public MiningBlock(AttackBlockEvent event) {
            this.pos = event.getPos();
            this.dir = event.getDir();
            startTime = System.currentTimeMillis();
        }

        public boolean mine() {
            if (pos.getSquaredDistance(mc.player.getEntityPos()) > Math.pow(mc.player.getBlockInteractionRange(), 2)
                    && cancelOutOfReach.getValue()) {
                return false;
            }

            if (dontBreak.getValue() && mc.player.getMainHandStack().getMaxDamage() - mc.player.getMainHandStack().getDamage() <= 1) {
                ChatUtil.warn("pico por romperse");
                toggle();
                return false;
            }

            mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, dir, sequence));
            mining = true;
            mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, dir, sequence));

            return true;
        }

        public void render(Render3DEvent event) {
            VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
            if (shape.isEmpty()) return;

            RenderUtil.drawVoxelShapeOutline(
                    event.getMatrices(),
                    shape,
                    pos,
                    color.getValue(),
                    lineWidth.getFloatValue(),
                    false
            );
        }

        public boolean shouldRetry() {
            return mining && (System.currentTimeMillis() - startTime >= retryTime.getValue());
        }
    }

    private enum ClickAction {
        UNSELECT("deseleccionar"),
        ADD_PRIORITY("agregar prioridad"),
        NONE("nada");

        private final String name;
        ClickAction(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}