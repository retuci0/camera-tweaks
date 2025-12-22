package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.CameraMixin
 * @see me.retucio.camtweaks.mixin.EntityMixin
 */

public class Freelook extends Module {

    public EnumSetting<CameraMode> mode = addSetting(new EnumSetting<>("cámara a mover", "elige cual cámara mover con el ratón y cuál se queda estática",
            CameraMode.class, CameraMode.CAMERA));

    public NumberSetting arrowSens = addSetting(new NumberSetting("sensibilidad de las flechas", "cuánto rotan la cámara estática las flechas (0 para desactivar)",
            4, 0, 10, 0.1));

    public NumberSetting mouseSens = addSetting(new NumberSetting("sensibilidad del ratón", ".",
            0.1, 0, 2, 0.05));

    private float yaw;
    private float pitch;
    private Perspective prevPerspective;

    public Freelook() {
        super("perspectiva libre", "te permite mover la cámara de la perspectiva sin mover la del jugador, o vice versa");
        assignKey(GLFW.GLFW_KEY_B);

        keyMode.setDefaultValue(KeyModes.HOLD);
        keyMode.setValue(KeyModes.HOLD);

        notify.setDefaultValue(false);
        notify.setEnabled(false);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        prevPerspective = mc.options.getPerspective();
        if (prevPerspective != Perspective.THIRD_PERSON_BACK)
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.options.getPerspective() != prevPerspective)
            mc.options.setPerspective(prevPerspective);
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (arrowSens.getValue() > 0) {
            for (int i = 0; i < (arrowSens.getValue() * 2); i++) {
                switch (mode.getValue()) {
                    case PLAYER -> {
                        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT)) yaw -= 0.5f;
                        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_RIGHT)) yaw += 0.5f;
                        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_UP)) pitch -= 0.5f;
                        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_DOWN)) pitch += 0.5f;
                    } case CAMERA -> {
                        float yaw = mc.player.getYaw();
                        float pitch = mc.player.getPitch();

                        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT)) yaw -= 0.5f;
                        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_RIGHT)) yaw += 0.5f;
                        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_UP)) pitch -= 0.5f;
                        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_DOWN)) pitch += 0.5f;

                        mc.player.setYaw(yaw);
                        mc.player.setPitch(pitch);
                    }
                }
            }
        }

        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch(), -90, 90));
        pitch = MathHelper.clamp(pitch, -90, 90);
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public enum CameraMode {
        PLAYER("jugador"),
        CAMERA("cámara");

        private final String name;
        CameraMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
