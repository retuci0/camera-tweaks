package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.RenderWorldEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.render.RenderUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

public class BreakingProgress extends Module {

    public EnumSetting<BreakMode> breakMode = addSetting(new EnumSetting<>("modo de minado", "elige si el cubo se encoge o se dilata",
            BreakMode.class, BreakMode.INWARDS));

    public BooleanSetting outlines = addSetting(new BooleanSetting("contorno", "renderizar contorno", true));
    public ColorSetting outlineColor = addSetting(new ColorSetting("color del contorno", "color del contorno",
            new Color(0, 255, 0, 200), false));
    public NumberSetting lineWidth = addSetting(new NumberSetting("grosor de línea", "grosor de las líneas del contorno", 1, 1, 5, 0.1));

    public BooleanSetting fillings = addSetting(new BooleanSetting("relleno", "renderizar relleno", true));
    public ColorSetting fillingColor = addSetting(new ColorSetting("color del relleno", "color del relleno",
            new Color(0, 255, 0, 60), false));

    public BreakingProgress() {
        super("progreso de minado", "te muestra el progreso de minado de un bloque de manera más visible");
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.interactionManager == null || mc.world == null) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult hitResult)) return;
        BlockPos pos = hitResult.getBlockPos();

        int breakingProgress = mc.interactionManager.getBlockBreakingProgress();
        if (breakingProgress <= 0) return;
        float progress = breakingProgress / 10f;

        VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
        if (shape.isEmpty()) return;

        Box box = shape.getBoundingBox().offset(pos);

        float shrinkFactor = breakMode.is(BreakMode.INWARDS)
                ? 1f - progress
                : progress;

        double centerX = (box.minX + box.maxX) / 2;
        double centerY = (box.minY + box.maxY) / 2;
        double centerZ = (box.minZ + box.maxZ) / 2;

        double shrunkX = (box.maxX - box.minX) / 2 * shrinkFactor;
        double shrunkY = (box.maxY - box.minY) / 2 * shrinkFactor;
        double shrunkZ = (box.maxZ - box.minZ) / 2 * shrinkFactor;

        Box scaledBox = new Box(
                centerX - shrunkX, centerY - shrunkY, centerZ - shrunkZ,
                centerX + shrunkX, centerY + shrunkZ, centerZ + shrunkZ
        );

        if (outlines.isEnabled()) RenderUtil.drawOutlineBox(event.getMatrices(), scaledBox, outlineColor.getColor(), lineWidth.getFloatValue());
        if (fillings.isEnabled()) RenderUtil.drawFilledBox(event.getMatrices(), scaledBox, fillingColor.getColor());
    }

    public enum BreakMode {
        INWARDS("para dentro"),
        OUTWARDS("para fuera");

        private final String name;
        BreakMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
