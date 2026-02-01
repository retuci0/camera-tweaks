package me.retucio.sputnik.module.modules.camera;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

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
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        if (direction.is(RotationDirection.CLOCKWISE))
            yaw += interval.getFloatValue();
        if (direction.is(RotationDirection.COUNTERCLOCKWISE))
            yaw -= interval.getFloatValue();

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                yaw,
                mc.player.getPitch(),
                mc.player.isOnGround(),
                mc.player.horizontalCollision
        ));
    }

    @SubscribeEvent
    private void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround packet
                && packet.getYaw(yaw) != yaw) event.cancel();
    }

    private enum RotationDirection {
        CLOCKWISE("sentido horario"),
        COUNTERCLOCKWISE("sentido antihorario");

        private final String name;
        RotationDirection(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
