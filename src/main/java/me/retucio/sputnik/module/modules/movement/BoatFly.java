package me.retucio.sputnik.module.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.retucio.sputnik.event.BoatMoveEvent;
import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.math.Vec3d;

public class BoatFly extends Module {

    public NumberSetting speed = sgGeneral.add(new NumberSetting(
            "velocidad horizontal",
            "velocidad a la que moverse horizontalmente",
            2,
            0,
            5,
            0.1
    ));

    public NumberSetting vSpeed = sgGeneral.add(new NumberSetting(
            "velocidad vertical",
            "velocidad a la que moverse verticalmente",
            1,
            0,
            5,
            0.1
    ));

    public NumberSetting stepHeight = sgGeneral.add(new NumberSetting(
            "altura de escalonado",
            "como escalones pero pal barco",
            0.5,
            0,
            1,
            0.1
    ));

    public BooleanSetting cancelPackets = sgGeneral.add(new BooleanSetting(
            "cancelar paquetes",
            "cancelar paquetes de movimiento del barco enviados por el servidor",
            false
    ));

    public BoatFly() {
        super("barco volador", "convierte tu bote de acacia en un Lockheed F-117 Nighthawk", Category.MOVEMENT);
    }

    @SubscribeEvent
    private void onBoatMove(BoatMoveEvent event) {
        if (event.getBoat().getControllingPassenger() != mc.player) return;
        event.getBoat().setYaw(mc.player.getYaw());

        float yaw = event.getBoat().getYaw();
        int dx = 0, dy = 0, dz = 0;

        if (mc.options.rightKey.isPressed()) dx++;
        if (mc.options.leftKey.isPressed()) dx--;

        if (mc.options.jumpKey.isPressed()) dy++;
        if (mc.options.sprintKey.isPressed()) dy--;
        // ctrl para bajar, en vez de shift, porque si no te desmontas

        if (mc.options.backKey.isPressed()) dz++;
        if (mc.options.forwardKey.isPressed()) dz--;

        double speed = this.speed.getValue();
        double sin = Math.sin(Math.toRadians(yaw));
        double cos = Math.cos(Math.toRadians(yaw));

        double x = speed * dz * sin;
        double y = speed * dy;
        double z = speed * dz * -cos;

        x += speed * dx * -cos;
        z += speed * dx * -sin;

        event.getBoat().setVelocity(new Vec3d(x, y, z));
    }

    @Subscribe
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof VehicleMoveS2CPacket && cancelPackets.getValue()) {
            event.cancel();
        }
    }
}
