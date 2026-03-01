package me.retucio.sputnik.module.modules.render;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.MiscUtil;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author retucio
 */

public class Breadcrumbs extends Module {

    private final NumberSetting lifeTime = sgGeneral.add(new NumberSetting(
            "esperanza de vida",
            "cuánto duran los trazos",
            10,
            0.1,
            40,
            0.1
    ));

    private final ColorSetting color = sgGeneral.add(new ColorSetting(
            "color",
            "color a usar",
            Colors.withAlpha(Colors.mainColor, 102),
            true
    ));

    private final NumberSetting lineWidth = sgGeneral.add(new NumberSetting(
            "grosor",
            "grosor de las líneas",
            1,
            0.1,
            10,
            0.1
    ));

    private final BooleanSetting fade = sgGeneral.add(new BooleanSetting(
            "gradiente",
            "gradiente de opacidad del recorrido basado en el tiempo pasado",
            true
    ));

    private final List<Breadcrumb> crumbs = new ArrayList<>();
    private Vec3d lastPos = null;

    public Breadcrumbs() {
        super("recorrido", "muestra tu recorrido", Category.RENDER);
    }

    @Override
    public void onDisable() {
        crumbs.clear();
        lastPos = null;
        super.onDisable();
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        Iterator<Breadcrumb> iterator = crumbs.iterator();
        while (iterator.hasNext()) {
            Breadcrumb crumb = iterator.next();

            Color c = fade.getValue() ? Colors.withAlpha(color.getValue(), (int) ((1 - ((float) crumb.age / (float) crumb.lifeTime)) * 255)) : color.getValue();
            RenderUtil.drawLine(event.getMatrices(), crumb.from, crumb.to, c, lineWidth.getFloatValue());

            crumb.age++;
            if (crumb.age >= crumb.lifeTime) {
                iterator.remove();
            }
        }

        if (lastPos != null) {
            crumbs.add(new Breadcrumb(lastPos, mc.player.getEntityPos(), (int) (lifeTime.getValue() * 20)));
        }
        lastPos = mc.player.getEntityPos();
    }


    private static class Breadcrumb {
        Vec3d from, to;
        int lifeTime, age;

        public Breadcrumb(Vec3d from, Vec3d to, int lifeTime) {
            this.from = from;
            this.to = to;
            this.lifeTime = lifeTime;
            this.age = 0;
        }
    }
}
