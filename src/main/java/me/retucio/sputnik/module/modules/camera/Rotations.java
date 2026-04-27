package me.retucio.sputnik.module.modules.camera;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.render.ChangeRotationEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * @author retucio
 */

public class Rotations extends Module {

    private final NumberSetting yaw = sgGeneral.add(new NumberSetting("guiñada", "eje vertical - giro horizontal (yaw)", 0, -180, 180, 1));
    private final NumberSetting pitch = sgGeneral.add(new NumberSetting("cabeceo", "eje horizontal - giro vertical (pitch)", 0, -90, 90, 1));

    private final BooleanSetting smooth = sgGeneral.add(new BooleanSetting("evitar movimiento", "cancela todo movimiento de la cámara", false));
    private final BooleanSetting serverSide = sgGeneral.add(new BooleanSetting("serverside", "espamea paquetes de rotación al servidor", false));

    public Rotations() {
        super("rotaciones",
                "te permite forzar una rotación específica",
                Category.CAMERA);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        mc.player.setYRot(yaw.getFloatValue());
        mc.player.setXRot(pitch.getFloatValue());

        if (serverSide.getValue())
            mc.player.connection.send(
                    new ServerboundMovePlayerPacket.Rot(
                        yaw.getFloatValue(),
                        pitch.getFloatValue(),
                        mc.player.onGround(),
                        mc.player.horizontalCollision
                    )
            );
    }

    @EventListener
    private void onChangeRotation(ChangeRotationEvent event) {
        if (smooth.getValue()
                && (event.getYaw() != yaw.getFloatValue() || event.getPitch() != pitch.getFloatValue())){
            event.cancel();
        }
    }
}
