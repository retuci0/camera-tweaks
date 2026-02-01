package me.retucio.sputnik.module.modules.network;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.interact.AttackBlockEvent;
import me.retucio.sputnik.event.events.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.ArrayList;
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

    @Override
    public void onDisable() {
        miningBlocks.clear();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        Iterator<MiningBlock> iterator = miningBlocks.iterator();
        while (iterator.hasNext()) {
            MiningBlock block = iterator.next();

            if (mc.world.getBlockState(block.pos).isAir()) {
                iterator.remove();
                continue;
            }

            if (!block.mining) {
                block.mine();
            } else {
                if (mc.player.age - block.startTime >= retryTime.getValue()) {
                    block.startTime = mc.player.age;
                    block.mining = false;
                }
            }
        }
    }

    @SubscribeEvent
    private void onAttackBlock(AttackBlockEvent event) {
        event.cancel();
        if (!isMining(event.getPos())) {
            miningBlocks.add(new MiningBlock(event));
        }
    }

    @SubscribeEvent
    private void onRenderWorld(Render3DEvent event) {
        for (MiningBlock block : miningBlocks) {
            block.render(event);
        }
    }

    private boolean isMining(BlockPos pos) {
        for (MiningBlock block : miningBlocks) {
            if (block.pos.equals(pos)) return true;
        }
        return false;
    }

    private class MiningBlock {

        BlockPos pos;
        Direction dir;
        boolean mining;
        long startTime;

        public MiningBlock(AttackBlockEvent event) {
            this.pos = event.getPos();
            this.dir = event.getDir();
            mining = false;
            startTime = mc.player.age;
        }

        public void mine() {
            if (pos.getSquaredDistance(mc.player.getEntityPos()) > Math.pow(mc.player.getBlockInteractionRange(), 2)
                    && cancelOutOfReach.getValue()) {
                return;
            }

            mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, dir, sequence));
            mining = true;
            mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, dir, sequence));
        }

        public void render(Render3DEvent event) {
            VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);

            RenderUtil.drawVoxelShapeOutline(
                    event.getMatrices(),
                    shape,
                    pos,
                    color.getValue(),
                    lineWidth.getFloatValue(),
                    false
            );
        }
    }
}