package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.MouseScrollEvent;
import me.retucio.camtweaks.event.events.PerspectiveChangeEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.*;
import me.retucio.camtweaks.util.KeyUtil;
import org.lwjgl.glfw.GLFW;

// continúa en CameraMixin
public class PerspectivePlus extends Module {

    private double distance;

    // ajustes
    public BooleanSetting clip = addSetting(new BooleanSetting(
            "clip", "atravesar bloques con la cámara", true));

    public NumberSetting defaultDistance = addSetting(new NumberSetting(
            "distancia", "distancia a la que está la cámara del jugador por defecto",
            4, 1, 20, 0.2));

    public NumberSetting scrollSens = addSetting(new NumberSetting(
            "sensibilidad del scroll", "sensibilidad de la rueda del ratón (0 para desactivar)",
            1, 0, 8, 0.1));

    public KeySetting scrollKey = addSetting(new KeySetting("tecla del scroll", "qué tecla mantener para usar la rueda del ratón", GLFW.GLFW_KEY_LEFT_ALT));

    public PerspectivePlus() {
        super("perspectiva plus", "añade cositas chulas al cambio de perspectiva");
    }

    @Override
    public void onEnable() {
        distance = defaultDistance.getValue();
        super.onEnable();
    }

    @SubscribeEvent
    public void onPerspectiveChange(PerspectiveChangeEvent event) {
        distance = defaultDistance.getValue();
    }

    @SubscribeEvent
    public void onMouseScroll(MouseScrollEvent event) {
        if (mc.options.getPerspective() == net.minecraft.client.option.Perspective.FIRST_PERSON
            || mc.currentScreen != null || scrollSens.getValue() <= 0) return;

        if (KeyUtil.isKeyDown(scrollKey.getKey())) {
            distance -= event.getVertical() / 4 * (scrollSens.getValue() * distance);
            event.cancel();
        }
    }

    public double getDistance() {
        return distance;
    }
}
