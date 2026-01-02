package me.retucio.camtweaks.module.modules.render;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.RenderWorldEvent;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.ListSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.Lists;
import me.retucio.camtweaks.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class BlockESP extends Module {

    public ListSetting<Block> blocks = addSetting(new ListSetting<>("bloques", "bloques a resaltar",
            Lists.blockList, Lists.allFalse(Lists.blockList), Lists.blockNames));

    public NumberSetting radius = addSetting(new NumberSetting("radio", "radio de búsqueda de bloques", 24, 2, 128, 1));

    public BooleanSetting outlines = addSetting(new BooleanSetting("contorno", "renderizar contorno de bloques", true));
    public ColorSetting outlineColor = addSetting(new ColorSetting("color del contorno", "color del contorno", Colors.CELESTE, false));
    public NumberSetting lineWidth = addSetting(new NumberSetting("grosor del contorno", "grosor de las líneas del contorno", 1, 1, 5, 0.2));

    public BooleanSetting fillings = addSetting(new BooleanSetting("relleno", "renderizar relleno de bloques", true));
    public ColorSetting fillingColor = addSetting(new ColorSetting("color del relleno", "color del relleno", new Color(57, 177, 215, 67), false));


    public BlockESP() {
        super("resaltado de bloques",
                "te muestra la posición de cierto tipo de bloque renderizando una caja con su forma",
                Category.RENDER);

        outlines.onUpdate(v -> {
            outlineColor.setVisible(v);
            lineWidth.setVisible(v);
        });

        fillings.onUpdate(v -> fillingColor.setVisible(v));
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null || mc.world == null) return;

        for (Block block : blocks.getOptions()) {
            if (blocks.isEnabled(block)) {
                List<BlockPos> blocksToHighlight = getNearbyBlocks(block);
                for (BlockPos pos : blocksToHighlight) {
                    if (outlines.isEnabled()) {
                        RenderUtil.drawVoxelShapeOutline(
                                event.getMatrices(),
                                getVoxelShape(pos),
                                pos,
                                outlineColor.getColor(),
                                lineWidth.getFloatValue());
                    }
                    if (fillings.isEnabled()) {
                        RenderUtil.drawVoxelShapeFilled(
                                event.getMatrices(),
                                getVoxelShape(pos),
                                pos,
                                fillingColor.getColor());
                    }
                }
            }
        }
    }

    private List<BlockPos> getNearbyBlocks(Block block) {
        List<BlockPos> blockPosList = new ArrayList<>();

        for (int i = -radius.getIntValue(); i < radius.getIntValue(); i++) {
            for (int j = -radius.getIntValue(); j < radius.getIntValue(); j++) {
                for (int k = -radius.getIntValue(); k < radius.getIntValue(); k++) {
                    BlockPos pos = mc.player.getBlockPos().add(new Vec3i(i, j, k));
                    if (mc.world.getBlockState(pos).getBlock() == block) {
                        blockPosList.add(pos);
                    }
                }
            }
        }
        return blockPosList;
    }

    private VoxelShape getVoxelShape(BlockPos pos) {
        return mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
    }
}
