package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.Lists;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class EntityESP extends Module {

    private final SettingGroup sgRender = addSg(new SettingGroup("cajas", true));

    public final EnumSetting<ESPMode> mode = sgGeneral.add(new EnumSetting<>(
            "modo",
            "modo de renderizado",
            ESPMode.class,
            ESPMode.BOX
    ));

    public final ListSetting<EntityType<?>> entities = sgGeneral.add(new ListSetting<>(
            "entidades",
            "entidades a resaltar",
            Lists.entityList,
            Lists.allFalse(Lists.entityList),
            Lists.entityNames
    ));

    public final ColorSetting glowColor = sgRender.add(new ColorSetting(
            "color",
            "color del brillo",
            Colors.RED,
            false
    ));

    private final BooleanSetting outlines = sgRender.add(new BooleanSetting(
            "contorno",
            "contorno de las cajas",
            true
    ));

    private final ColorSetting outlineColor = sgRender.add(new ColorSetting(
            "color del contorno",
            "color para el contorno",
            Colors.withAlpha(Colors.RED.brighter(), 100),
            false
    ));

    private final BooleanSetting filling = sgRender.add(new BooleanSetting(
            "relleno",
            "relleno de las cajas",
            true
    ));

    private final ColorSetting fillingColor = sgRender.add(new ColorSetting(
            "color del relleno",
            "color para el relleno",
            Colors.withAlpha(Colors.RED, 70),
            false
    ));

    public EntityESP() {
        super("resaltado de entidades", "resalta el contorno de las entidades seleccionadas a través de bloques", Category.RENDER);
        mode.onUpdate(v -> {
            glowColor.setVisible(v == ESPMode.GLOW);
            outlines.setVisible(v == ESPMode.BOX);
            filling.setVisible(v == ESPMode.BOX);
            outlineColor.setVisible(outlines.isVisible());
            fillingColor.setVisible(filling.isVisible());
        });

        outlines.onUpdate(v -> outlineColor.setVisible(v));
        filling.onUpdate(v -> fillingColor.setVisible(v));
    }

    @SubscribeEvent
    private void onRenderWorld(Render3DEvent event) {
        if (mode.is(ESPMode.BOX)) drawBoxes(event.getMatrices());
    }

    private void drawBoxes(MatrixStack matrices) {
        for (Entity entity : mc.world.getEntities()) {
            if (!entities.isEnabled(entity.getType())) continue;
            if (filling.getValue()) RenderUtil.drawFilledBox(matrices, entity.getBoundingBox(), fillingColor.getValue(), false);
            if (outlines.getValue()) RenderUtil.drawOutlineBox(matrices, entity.getBoundingBox(), outlineColor.getValue(), 2, false);
        }
    }


    public enum ESPMode {
        BOX("caja 3d"),
        GLOW("efecto de brillo");

        private final String name;
        ESPMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
