package me.retucio.sputnik.module.modules.render;

import com.github.retucio.neutrino.EventListener;
import com.ibm.icu.impl.TimeZoneGenericNames;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.Lists;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Map;

public class Tracers extends Module {

    private final SettingGroup sgColors = addSg(new SettingGroup("colores", false));

    private final Map<EntityType<?>, Boolean> defaultEntities = Lists.allFalse(Lists.entityList);
    private final ListSetting<EntityType<?>> entities = sgGeneral.add(new ListSetting<>(
            "entidades",
            "entidades a las que apuntar",
            Lists.entityList,
            defaultEntities,
            Lists.entityNames
    ));

    private final NumberSetting lineWidth = sgGeneral.add(new NumberSetting(
            "grosor de línea",
            "grosor de las líneas",
            1.5,
            0.1,
            10,
            0.2
    ));

    private final BooleanSetting self = sgGeneral.add(new BooleanSetting(
            "uno mismo",
            "apuntarse a uno mismo en cámara libre o así",
            true
    ));

    private final ColorSetting playerColor= sgColors.add(new ColorSetting(
            "jugadores",
            "color para jugadores",
            Colors.RED, false
    ));

    private final ColorSetting animalsColor = sgColors.add(new ColorSetting(
            "animales",
            "color para animales pasivos",
            Colors.YELLOW, false
    ));

    private final ColorSetting monstersColor = sgColors.add(new ColorSetting(
            "monstruos",
            "color para monstruos",
            Colors.BROWN, false
    ));

    private final ColorSetting ambientColor = sgColors.add(new ColorSetting(
            "ambiente",
            "color para entidades ambiente",
            Colors.SILVER, false
    ));

    private final ColorSetting waterColor = sgColors.add(new ColorSetting(
            "agua",
            "color para bichos del agua",
            Colors.BLUE, false
    ));

    private final ColorSetting miscColor = sgColors.add(new ColorSetting(
            "misc.",
            "color para entidades misceláneas",
            Colors.BROWN, false
    ));

    public Tracers() {
        super("trazos", "trazar líneas de la cámara a las entidades seleccionadas", Category.RENDER);
        defaultEntities.replace(EntityType.PLAYER, true);
        entities.setDefaultValue(defaultEntities);
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        for (Entity entity : mc.world.getEntities()) {
            Color color = getColor(entity);
            if (color == null) continue;
            Vec3d interpolatedPos = entity.getLerpedPos(event.getTickDelta()).add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
            RenderUtil.drawTracer(event.getMatrices(), interpolatedPos, color, lineWidth.getFloatValue());
        }
    }

    private Color getColor(Entity entity) {
        if (!entities.isEnabled(entity.getType())) return null;
        if (!self.getValue() && mc.player == entity) return null;
        if (entity instanceof PlayerEntity) return playerColor.getValue();

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE -> animalsColor.getValue().getAlpha() > 0 ? animalsColor.getValue() : null;
            case MONSTER -> monstersColor.getValue().getAlpha() > 0 ? monstersColor.getValue() : null;
            case AMBIENT, WATER_AMBIENT -> ambientColor.getValue().getAlpha() > 0 ? ambientColor.getValue() : null;
            case WATER_CREATURE, UNDERGROUND_WATER_CREATURE -> waterColor.getValue().getAlpha() > 0 ? waterColor.getValue() : null;
            case MISC -> miscColor.getValue().getAlpha() > 0 ? miscColor.getValue() : null;
            default -> null;
        };
    }
}
