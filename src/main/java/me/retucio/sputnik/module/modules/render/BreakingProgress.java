package me.retucio.sputnik.module.modules.render;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.*;


public class BreakingProgress extends Module {

    private final SettingGroup sgOulines = addSg(new SettingGroup("contorno", true));
    private final SettingGroup sgFilling = addSg(new SettingGroup("relleno", true));

    private final EnumSetting<BreakMode> breakMode = sgGeneral.add(new EnumSetting<>("modo de minado", "elige si el cubo se encoge o se dilata",
            BreakMode.class, BreakMode.INWARDS));

    private final BooleanSetting outlines = sgOulines.add(new BooleanSetting("contorno", "renderizar contorno", true));
    private final ColorSetting outlineColor = sgOulines.add(new ColorSetting("color del contorno", "color del contorno",
            new Color(0, 255, 0, 200), false));
    private final NumberSetting lineWidth = sgOulines.add(new NumberSetting("grosor de línea", "grosor de las líneas del contorno", 1, 1, 5, 0.1));

    private final BooleanSetting fillings = sgFilling.add(new BooleanSetting("relleno", "renderizar relleno", true));
    private final ColorSetting fillingColor = sgFilling.add(new ColorSetting("color del relleno", "color del relleno",
            new Color(0, 255, 0, 60), false));


    public BreakingProgress() {
        super("progreso de minado",
                "te muestra el progreso de minado de un bloque de manera más visible",
                Category.RENDER);
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (mc.gameMode == null || mc.level == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hitResult)) return;
        BlockPos pos = hitResult.getBlockPos();

        int breakingProgress = mc.gameMode.getDestroyStage();
        if (breakingProgress <= 0) return;
        float progress = breakingProgress / 10f;

        VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
        if (shape.isEmpty()) return;

        AABB box = shape.bounds().move(pos);

        float shrinkFactor = breakMode.is(BreakMode.INWARDS)
                ? 1f - progress
                : progress;

        AABB scaledBox = scaleBox(box, shrinkFactor);

        if (outlines.getValue()) RenderUtil.drawOutlineBox(event.getMatrices(), scaledBox, outlineColor.getValue(), lineWidth.getFloatValue(), false);
        if (fillings.getValue()) RenderUtil.drawFilledBox(event.getMatrices(), scaledBox, fillingColor.getValue(), false);
    }

    private static AABB scaleBox(AABB box, float shrinkFactor) {
        double centerX = (box.minX + box.maxX) / 2;
        double centerY = (box.minY + box.maxY) / 2;
        double centerZ = (box.minZ + box.maxZ) / 2;

        double shrunkX = (box.maxX - box.minX) / 2 * shrinkFactor;
        double shrunkY = (box.maxY - box.minY) / 2 * shrinkFactor;
        double shrunkZ = (box.maxZ - box.minZ) / 2 * shrinkFactor;

        return new AABB(
                centerX - shrunkX, centerY - shrunkY, centerZ - shrunkZ,
                centerX + shrunkX, centerY + shrunkZ, centerZ + shrunkZ
        );
    }

    private enum BreakMode {
        INWARDS("para dentro"),
        OUTWARDS("para fuera");

        private final String name;
        BreakMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
