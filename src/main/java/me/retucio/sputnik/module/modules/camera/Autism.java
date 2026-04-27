package me.retucio.sputnik.module.modules.camera;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;


/**
 * @author retucio
 */

public class Autism extends Module {

    private final NumberSetting interval = sgGeneral.add(new NumberSetting(
            "intervalo",
            "intervalo de rotación",
            18,
            1,
            90,
            1
    ));


    private final EnumSetting<RotationDirection> direction = sgGeneral.add(new EnumSetting<>(
            "dirección",
            "dirección de la rotación",
            RotationDirection.class,
            RotationDirection.CLOCKWISE
    ));

    private float yaw;

    public Autism() {
        super("autismo", "jeje", Category.CAMERA);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) return;

        if (direction.is(RotationDirection.CLOCKWISE))
            yaw += interval.getFloatValue();
        if (direction.is(RotationDirection.COUNTERCLOCKWISE))
            yaw -= interval.getFloatValue();

        mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                yaw,
                mc.player.getXRot(),
                mc.player.onGround(),
                mc.player.horizontalCollision
        ));
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundMovePlayerPacket.Rot packet
                && packet.getYRot(yaw) != yaw) event.cancel();
    }

    private enum RotationDirection {
        CLOCKWISE("sentido horario"),
        COUNTERCLOCKWISE("sentido antihorario");

        private final String name;
        RotationDirection(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
