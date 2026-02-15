package me.retucio.sputnik.module.modules.player;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.render.RenderHeldItemEvent;
import me.retucio.sputnik.event.render.RenderArmEvent;
import me.retucio.sputnik.mixin.mixins.render.HeldItemRendererMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/** continúa en:
 * @see HeldItemRendererMixin
 */

public class HandView extends Module {

    private final SettingGroup sgAnimations = addSg(new SettingGroup("animaciones", false));
    private final SettingGroup sgMainHand = addSg(new SettingGroup("mano principal", false));
    private final SettingGroup sgOffhand = addSg(new SettingGroup("mano secundaria", false));
    private final SettingGroup sgArm = addSg(new SettingGroup("brazo", false));

    // ajustes de animaciones
    public final BooleanSetting oldAnimations = sgAnimations.add(new BooleanSetting("animaciones viejas", "como las de la 1.8, al pegar", false));
    public final BooleanSetting skipSwapping = sgAnimations.add(new BooleanSetting("no swap", "se salta la animación de cambiar items de mano", false));
    public final BooleanSetting noFood = sgAnimations.add(new BooleanSetting("no comer", "no renderiza la animación de comer", false));

    // ajustes de mano principal
    private final NumberSetting scaleMainX = sgMainHand.add(new NumberSetting("escala de la mano principal (X)", ".",
            1, 0.01, 5, 0.1));
    private final NumberSetting scaleMainY = sgMainHand.add(new NumberSetting("escala de la mano principal (Y)", ".",
            1, 0.01, 5, 0.1));
    private final NumberSetting scaleMainZ = sgMainHand.add(new NumberSetting("escala de la mano principal (Z)", ".",
            1, 0.01, 5, 0.1));

    private final NumberSetting posMainX = sgMainHand.add(new NumberSetting("posición de la mano principal (X)", ".",
            0, -3, 3, 0.1));
    private final NumberSetting posMainY = sgMainHand.add(new NumberSetting("posición de la mano principal (Y)", ".",
            0, -3, 3, 0.1));
    private final NumberSetting posMainZ = sgMainHand.add(new NumberSetting("posición de la mano principal (Z)", ".",
            0, -3, 3, 0.1));

    private final NumberSetting rotMainX = sgMainHand.add(new NumberSetting("rotación de la mano principal (X)", ".",
            0, -180, 180, 1));
    private final NumberSetting rotMainY = sgMainHand.add(new NumberSetting("rotación de la mano principal (Y)", ".",
            0, -180, 180, 1));
    private final NumberSetting rotMainZ = sgMainHand.add(new NumberSetting("rotación de la mano principal (Z)", ".",
            0, -180, 180, 1));

    // ajustes de mano secundaria
    private final NumberSetting scaleOffX = sgOffhand.add(new NumberSetting("escala de la mano secundaria (X)", ".",
            1, 0.01, 5, 0.1));
    private final NumberSetting scaleOffY = sgOffhand.add(new NumberSetting("escala de la mano secundaria (Y)", ".",
            1, 0.01, 5, 0.1));
    private final NumberSetting scaleOffZ = sgOffhand.add(new NumberSetting("escala de la mano secundaria (Z)", ".",
            1, 0.01, 5, 0.1));

    private final NumberSetting posOffX = sgOffhand.add(new NumberSetting("posición de la mano secundaria (X)", ".",
            0, -3, 3, 0.1));
    private final NumberSetting posOffY = sgOffhand.add(new NumberSetting("posición de la mano secundaria (Y)", ".",
            0, -3, 3, 0.1));
    private final NumberSetting posOffZ = sgOffhand.add(new NumberSetting("posición de la mano secundaria (Z)", ".",
            0, -3, 3, 0.1));

    private final NumberSetting rotOffX = sgOffhand.add(new NumberSetting("rotación de la mano secundaria (X)", ".",
            0, -180, 180, 1));
    private final NumberSetting rotOffY = sgOffhand.add(new NumberSetting("rotación de la mano secundaria (Y)", ".",
            0, -180, 180, 1));
    private final NumberSetting rotOffZ = sgOffhand.add(new NumberSetting("rotación de la mano secundaria (Z)", ".",
            0, -180, 180, 1));

    // brazo
    private final NumberSetting scaleArmX = sgArm.add(new NumberSetting("escala del brazo (X)", ".",
            1, 0.01, 5, 0.1));
    private final NumberSetting scaleArmY = sgArm.add(new NumberSetting("escala del brazo (Y)", ".",
            1, 0.01, 5, 0.1));
    private final NumberSetting scaleArmZ = sgArm.add(new NumberSetting("escala del brazo (Z)", ".",
            1, 0.01, 5, 0.1));

    private final NumberSetting posArmX = sgArm.add(new NumberSetting("posición del brazo (X)", ".",
            0, -3, 3, 0.1));
    private final NumberSetting posArmY = sgArm.add(new NumberSetting("posición del brazo (Y)", ".",
            0, -3, 3, 0.1));
    private final NumberSetting posArmZ = sgArm.add(new NumberSetting("posición del brazo (Z)", ".",
            0, -3, 3, 0.1));

    private final NumberSetting rotArmX = sgArm.add(new NumberSetting("rotación del brazo (X)", ".",
            0, -180, 180, 1));
    private final NumberSetting rotArmY = sgArm.add(new NumberSetting("rotación del brazo (Y)", ".",
            0, -180, 180, 1));
    private final NumberSetting rotArmZ = sgArm.add(new NumberSetting("rotación del brazo (Z)", ".",
            0, -180, 180, 1));

    public HandView() {
        super("manos",
                "modifica la manera en la que se renderizan las manos",
                Category.PLAYER);
    }

    @EventListener
    private void onRenderHand(RenderHeldItemEvent event) {
        switch (event.getHand()) {
            case MAIN_HAND -> {
                scale(event.getMatrices(), new Vec3d(scaleMainX.getValue(), scaleMainY.getValue(), scaleMainZ.getValue()));
                translate(event.getMatrices(), new Vec3d(posMainX.getValue(), posMainY.getValue(), posMainZ.getValue()));
                rotate(event.getMatrices(), new Vec3d(rotMainX.getValue(), rotMainY.getValue(), rotMainZ.getValue()));
            }
            case OFF_HAND -> {
                scale(event.getMatrices(), new Vec3d(scaleOffX.getValue(), scaleOffY.getValue(), scaleOffZ.getValue()));
                translate(event.getMatrices(), new Vec3d(posOffX.getValue(), posOffY.getValue(), posOffZ.getValue()));
                rotate(event.getMatrices(), new Vec3d(rotOffX.getValue(), rotOffY.getValue(), rotOffZ.getValue()));
            }
        }
    }

    @EventListener
    private void onRenderArm(RenderArmEvent event) {
        scale(event.getMatrices(), new Vec3d(scaleArmX.getValue(), scaleArmY.getValue(), scaleArmZ.getValue()));
        translate(event.getMatrices(), new Vec3d(posArmX.getValue(), posArmY.getValue(), posArmZ.getValue()));
        rotate(event.getMatrices(), new Vec3d(rotArmX.getValue(), rotArmY.getValue(), rotArmZ.getValue()));
    }

    private void rotate(MatrixStack matrix, Vec3d rotation) {
        matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) rotation.x));
        matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) rotation.y));
        matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) rotation.z));
    }

    private void scale(MatrixStack matrix, Vec3d scale) {
        matrix.scale((float) scale.x, (float) scale.y, (float) scale.z);
    }

    private void translate(MatrixStack matrix, Vec3d translation) {
        matrix.translate((float) translation.x, (float) translation.y, (float) translation.z);
    }
}
