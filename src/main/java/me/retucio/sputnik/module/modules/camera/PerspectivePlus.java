package me.retucio.sputnik.module.modules.camera;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.input.MouseScrollEvent;
import me.retucio.sputnik.event.render.PerspectiveChangeEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import org.lwjgl.glfw.GLFW;

// continúa en CameraMixin
public class PerspectivePlus extends Module {

    // ajustes
    private final SettingGroup sgScroll = addSg(new SettingGroup("scrolleo", true));

    public final BooleanSetting clip = sgGeneral.add(new BooleanSetting(
            "clip", "atravesar bloques con la cámara", true));

    private final NumberSetting defaultDistance = sgGeneral.add(new NumberSetting(
            "distancia", "distancia a la que está la cámara del jugador por defecto",
            4, 1, 20, 0.2));

    private final NumberSetting scrollSens = sgScroll.add(new NumberSetting(
            "sensibilidad del scroll", "sensibilidad de la rueda del ratón (0 para desactivar)",
            1, 0, 8, 0.1));

    private final KeySetting scrollKey = sgScroll.add(new KeySetting("tecla del scroll", "qué tecla mantener para usar la rueda del ratón",
            GLFW.GLFW_KEY_LEFT_ALT));

    private double distance;

    public PerspectivePlus() {
        super("perspectiva plus",
                "añade cositas chulas al cambio de perspectiva",
                Category.CAMERA);
    }

    @Override
    public void onEnable() {
        distance = defaultDistance.getValue();
        super.onEnable();
    }

    @EventListener
    private void onPerspectiveChange(PerspectiveChangeEvent event) {
        distance = defaultDistance.getValue();
    }

    @EventListener
    private void onMouseScroll(MouseScrollEvent event) {
        if (mc.options.getPerspective() == net.minecraft.client.option.Perspective.FIRST_PERSON
            || mc.currentScreen != null || scrollSens.getValue() <= 0) return;

        if (scrollKey.isDown()) {
            distance -= event.getVertical() / 4 * (scrollSens.getValue() * distance);
            event.cancel();
        }
    }

    public double getDistance() {
        return distance;
    }
}
