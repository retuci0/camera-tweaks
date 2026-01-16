package me.retucio.sputnik.module.modules.camera;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.GetFOVEvent;
import me.retucio.sputnik.event.events.MouseScrollEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.KeyUtil;
import org.lwjgl.glfw.GLFW;

// continúa en GameRendererMixin
public class Zoom extends Module {

    SettingGroup sgVisual = addSg(new SettingGroup("visual", true));
    SettingGroup sgScroll = addSg(new SettingGroup("scrolleo", true));
    SettingGroup sgCam = addSg(new SettingGroup("cámara", true));

    public NumberSetting defaultZoom = sgGeneral.add(new NumberSetting("zoom", "cantidad de zoom",
            6, 1, 10, 0.1));

    public BooleanSetting showHands = sgVisual.add(new BooleanSetting("mostrar manos", "esconde o muestra las manos al hacer zoom", true));
    public BooleanSetting showHUD = sgVisual.add(new BooleanSetting("mostrar HUD", "esconde o muestra los indicadores en pantalla", true));

    public NumberSetting scrollSens = sgScroll.add(new NumberSetting("sensibilidad del scroll", "sensibilidad de la rueda del ratón (0 para desactivar)",
            0.4, 0, 8, 0.1));
    public KeySetting scrollKey = sgScroll.add(new KeySetting("tecla del scroll", "qué tecla mantener para usar la rueda del ratón",
            GLFW.GLFW_KEY_LEFT_CONTROL));

    public NumberSetting mouseSensMultiplier = sgCam.add(new NumberSetting("sensibilidad", "multiplicador de la sensibilidad del ratón",
            0.4, 0, 1, 0.05));
    public BooleanSetting smoothCam = sgCam.add(new BooleanSetting("cámara cinemática", "usa la cámara cinemática mientras hagas zoom", false));

    private boolean prevSmoothCam;
    private double prevMouseSens;
    private double prevFov;
    private boolean prevHUD;
    private double value;

    public Zoom() {
        super("zoom",
                "lupa",
                Category.CAMERA,
                GLFW.GLFW_KEY_F);

        keyMode.setDefaultValue(KeyModes.HOLD);
        keyMode.setValue(KeyModes.HOLD);

        notify.setDefaultValue(false);
        notify.setValue(false);
    }

    @Override
    public void onEnable() {
        if (mc.options == null) return;

        prevSmoothCam = mc.options.smoothCameraEnabled;
        prevMouseSens = mc.options.getMouseSensitivity().getValue();
        prevFov = mc.options.getFov().getValue();
        prevHUD = mc.options.hudHidden;
        mc.options.hudHidden = !showHUD.getValue();

        value = defaultZoom.getValue();

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.options == null) return;

        mc.options.smoothCameraEnabled = prevSmoothCam;
        mc.options.getMouseSensitivity().setValue(prevMouseSens);
        mc.options.hudHidden = prevHUD;

        mc.worldRenderer.scheduleTerrainUpdate();

        super.onDisable();
    }

    @Override
    public void onTick() {
        mc.options.smoothCameraEnabled = smoothCam.getValue();
        if (!smoothCam.getValue())
            mc.options.getMouseSensitivity().setValue(prevMouseSens * mouseSensMultiplier.getValue());
    }

    @SubscribeEvent
    private void onMouseScroll(MouseScrollEvent event) {
        boolean key = scrollKey.getValue() == GLFW.GLFW_KEY_UNKNOWN || KeyUtil.isKeyDown(scrollKey.getValue());
        if (isEnabled() && scrollSens.getValue() > 0 && key) {
            value += event.getVertical() * 0.25 * (scrollSens.getValue() * value);
            if (value < 1) value = 1;

            event.cancel();
        }
    }

    @SubscribeEvent
    private void onGetFov(GetFOVEvent event) {
        event.setFov((float) (event.getFov() / value));

        if (prevFov != event.getFov()) mc.worldRenderer.scheduleTerrainUpdate();
        prevFov = event.getFov();
    }
}
