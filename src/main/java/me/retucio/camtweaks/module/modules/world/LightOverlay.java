package me.retucio.camtweaks.module.modules.world;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.RenderWorldEvent;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.render.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LightOverlay extends Module {

    public NumberSetting radius = addSetting(new NumberSetting("radio", "radio a tener en cuenta al renderizar superposición",
            16, 5, 128, 1));
    public NumberSetting yRadius = addSetting(new NumberSetting("rango vertical", "distancia vertical a tener en cuenta",
            4, 1, 16, 1));

    public NumberSetting interval = addSetting(new NumberSetting("intervalo", "cada cuántos ticks comprobar el nivel de luz",
            10, 1, 80, 1));

    public ColorSetting lightColor = addSetting(new ColorSetting("color de la luz", "color de los bloques con luz",
            new Color(0, 255, 0, 67), false));
    public ColorSetting darknessColor = addSetting(new ColorSetting("color de oscuridad", "color de bloques con 0 luz",
            new Color(255, 0, 0, 67), false));

    public BooleanSetting onWater = addSetting(new BooleanSetting("en agua", "mostrar también bloques cubiertos en agua", true));

    List<BlockPos> blocks = new ArrayList<>();

    public LightOverlay() {
        super("superposición de luz",
                "te muestra el nivel de luz en bloques, para prevenir mob spawns",
                Category.WORLD);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.age % interval.getIntValue() == 0) {
            blocks = findExposedBlocks(
                    radius.getIntValue(),
                    yRadius.getIntValue()
            );
        }

        for (BlockPos block : blocks) {
            if (mc.world.getBlockState(block).isAir() || !mc.world.getBlockState(block.up()).isAir()) continue;
            if (!mc.world.isTopSolid(block, mc.player) || !mc.world.getBlockState(block).isOpaque()) continue;
            int light = mc.world.getLightLevel(LightType.BLOCK, block.up());

            Color color = light == 0 ? darknessColor.getColor()
                    : new Color(
                            (lightColor.getR() / 15) * light,
                            (lightColor.getG() / 15) * light,
                            (lightColor.getB() / 15) * light,
                            lightColor.getA());

            RenderUtil.drawBlockFaceFilled(event.getMatrices(), block, Direction.UP, color, 0.001f);
        }
    }

    private List<BlockPos> findExposedBlocks(int radius, int yRadius) {
        List<BlockPos> results = new ArrayList<>();

        BlockPos pos = mc.player.getBlockPos();

        int minX = pos.getX() - radius;
        int maxX = pos.getX() + radius;
        int minY = Math.max(mc.world.getDimension().minY(), pos.getY() - yRadius);
        int maxY = Math.min(mc.world.getDimension().height() - 1, pos.getY() + yRadius);
        int minZ = pos.getZ() - radius;
        int maxZ = pos.getZ() + radius;

        maxY = Math.min(maxY, mc.world.getDimension().height() - 2);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos current = new BlockPos(x, y, z);
                    BlockPos above = current.up();

                    if (!mc.world.isAir(current) && (mc.world.isAir(above) || (mc.world.isWater(above) && onWater.isEnabled())))
                        results.add(current);
                }
            }
        }

        return results;
    }
}
