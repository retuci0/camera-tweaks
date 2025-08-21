package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.Subscribe;
import me.retucio.camtweaks.event.events.GetFOVEvent;
import me.retucio.camtweaks.event.events.MouseScrollEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

// continúa en GameRendererMixin
public class Zoom extends Module {

    public BooleanSetting showHands = new BooleanSetting("mostrar manos", "esconde o muestra las manos al hacer zoom", true);
    public BooleanSetting showHUD = new BooleanSetting("mostrar HUD", "esconde o muestra los indicadores en pantalla", true);

    public NumberSetting scrollSens = new NumberSetting("sensibilidad del scroll", "sensibilidad de la rueda del ratón (0 para desactivar)",
            0.4, 0, 8, 0.1);

    public KeySetting scrollKey = new KeySetting("tecla del scroll", "qué tecla mantener para usar la rueda del ratón", GLFW.GLFW_KEY_LEFT_CONTROL);

    public NumberSetting defaultZoom = new NumberSetting("zoom", "cantidad de zoom",
            6, 1, 10, 0.1);

    public NumberSetting mouseSensMultiplier = new NumberSetting("sensibilidad", "multiplicador de la sensibilidad del ratón",
            0.7, 0.01, 1, 0.05);

    public BooleanSetting smoothCam = new BooleanSetting("cámara cinemática", "usa la cámara cinemática mientras hagas zoom", false);

    private boolean prevSmoothCam;
    private double prevMouseSens;
    private double prevFov;
    private boolean prevHUD;
    private double value;

    public Zoom() {
        super("zoom", "lupa");
        super.keyMode.setValue(KeyModes.HOLD);
        super.keyMode.setDefaultValue(KeyModes.HOLD);
        super.notify.setEnabled(false);
        super.notify.setDefaultValue(false);
        setKey(GLFW.GLFW_KEY_F);
        addSettings(showHands, showHUD, scrollSens, scrollKey, defaultZoom, mouseSensMultiplier, smoothCam);
    }

    @Override
    public void onEnable() {
        CameraTweaks.EVENT_BUS.register(this);

        prevSmoothCam = mc.options.smoothCameraEnabled;
        prevMouseSens = mc.options.getMouseSensitivity().getValue();
        prevFov = mc.options.getFov().getValue();
        prevHUD = mc.options.hudHidden;
        mc.options.hudHidden = !showHUD.isEnabled();

        value = defaultZoom.getValue();
    }

    @Override
    public void onDisable() {
        CameraTweaks.EVENT_BUS.unregister(this);

        mc.options.smoothCameraEnabled = prevSmoothCam;
        mc.options.getMouseSensitivity().setValue(prevMouseSens);
        mc.options.hudHidden = prevHUD;

        mc.worldRenderer.scheduleTerrainUpdate();
    }

    @Override
    public void onTick() {
        mc.options.smoothCameraEnabled = smoothCam.isEnabled();
        if (!smoothCam.isEnabled())
            mc.options.getMouseSensitivity().setValue(prevMouseSens * mouseSensMultiplier.getValue());
    }

    @Subscribe
    private void onMouseScroll(MouseScrollEvent event) {
        if (scrollSens.getValue() > 0 && isEnabled() && KeyUtil.isKeyDown(scrollKey.getKey())) {
            value += event.getVertical() * 0.25 * (scrollSens.getValue() * value);
            if (value < 1) value = 1;

            event.cancel();
        }
    }

    @Subscribe
    private void onGetFov(GetFOVEvent event) {
        event.setFov((float) (event.getFov() / value));

        if (prevFov != event.getFov()) mc.worldRenderer.scheduleTerrainUpdate();
        prevFov = event.getFov();
    }
}
