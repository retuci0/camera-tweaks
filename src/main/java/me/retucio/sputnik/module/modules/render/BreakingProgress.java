package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

public class BreakingProgress extends Module {

    SettingGroup sgOulines = addSg(new SettingGroup("contorno", true));
    SettingGroup sgFilling = addSg(new SettingGroup("relleno", true));

    public EnumSetting<BreakMode> breakMode = sgGeneral.add(new EnumSetting<>("modo de minado", "elige si el cubo se encoge o se dilata",
            BreakMode.class, BreakMode.INWARDS));

    public BooleanSetting outlines = sgOulines.add(new BooleanSetting("contorno", "renderizar contorno", true));
    public ColorSetting outlineColor = sgOulines.add(new ColorSetting("color del contorno", "color del contorno",
            new Color(0, 255, 0, 200), false));
    public NumberSetting lineWidth = sgOulines.add(new NumberSetting("grosor de línea", "grosor de las líneas del contorno", 1, 1, 5, 0.1));

    public BooleanSetting fillings = sgFilling.add(new BooleanSetting("relleno", "renderizar relleno", true));
    public ColorSetting fillingColor = sgFilling.add(new ColorSetting("color del relleno", "color del relleno",
            new Color(0, 255, 0, 60), false));


    public BreakingProgress() {
        super("progreso de minado",
                "te muestra el progreso de minado de un bloque de manera más visible",
                Category.RENDER);
    }

    @SubscribeEvent
    public void onRenderWorld(Render3DEvent event) {
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

        if (outlines.getValue()) RenderUtil.drawOutlineBox(event.getMatrices(), scaledBox, outlineColor.getValue(), lineWidth.getFloatValue(), false);
        if (fillings.getValue()) RenderUtil.drawFilledBox(event.getMatrices(), scaledBox, fillingColor.getValue(), false);
    }

    public enum BreakMode {
        INWARDS("para dentro"),
        OUTWARDS("para fuera");

        private final String name;
        BreakMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
