package me.retucio.sputnik.module.modules.movement;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.interact.BoatMoveEvent;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.phys.Vec3;


public class BoatFly extends Module {

    private final NumberSetting speed = sgGeneral.add(new NumberSetting(
            "velocidad horizontal",
            "velocidad a la que moverse horizontalmente",
            2,
            0,
            5,
            0.1
    ));

    private final NumberSetting vSpeed = sgGeneral.add(new NumberSetting(
            "velocidad vertical",
            "velocidad a la que moverse verticalmente",
            1,
            0,
            5,
            0.1
    ));

    public final NumberSetting stepHeight = sgGeneral.add(new NumberSetting(
            "altura de escalonado",
            "como escalones pero pal barco",
            0.5,
            0,
            1,
            0.1
    ));

    private final BooleanSetting cancelPackets = sgGeneral.add(new BooleanSetting(
            "cancelar paquetes",
            "cancelar paquetes de movimiento del barco enviados por el servidor",
            false
    ));

    public BoatFly() {
        super("barco volador", "convierte tu bote de acacia en un Lockheed F-117 Nighthawk", Category.MOVEMENT);
    }

    @EventListener
    private void onBoatMove(BoatMoveEvent event) {
        if (event.getBoat().getControllingPassenger() != mc.player) return;
        event.getBoat().setYRot(mc.player.getYRot());

        float yaw = event.getBoat().getYRot();
        int dx = 0, dy = 0, dz = 0;

        if (mc.options.keyRight.isDown()) dx++;
        if (mc.options.keyLeft.isDown()) dx--;

        if (mc.options.keyJump.isDown()) dy++;
        if (mc.options.keySprint.isDown()) dy--;
        // ctrl para bajar, en vez de shift, porque si no te desmontas

        if (mc.options.keyDown.isDown()) dz++;
        if (mc.options.keyUp.isDown()) dz--;

        double speed = this.speed.getValue();
        double sin = Math.sin(Math.toRadians(yaw));
        double cos = Math.cos(Math.toRadians(yaw));

        double x = speed * dz * sin;
        double y = vSpeed.getValue() * dy;
        double z = speed * dz * -cos;

        x += speed * dx * -cos;
        z += speed * dx * -sin;

        event.getBoat().setDeltaMovement(new Vec3(x, y, z));
    }

    @EventListener
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ServerboundMoveVehiclePacket && cancelPackets.getValue()) {
            event.cancel();
        }
    }
}
