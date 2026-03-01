package me.retucio.sputnik.module.modules.movement;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.mixin.accessors.EntityVelocityUpdateS2CPacketAccessor;
import me.retucio.sputnik.mixin.accessors.ExplosionS2CPacketAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;


/** continúa en:
 * @see me.retucio.sputnik.mixin.mixins.entity.EntityMixin
 * @see me.retucio.sputnik.mixin.mixins.entity.LivingEntityMixin
 *
 * @author retucio
 */

public class Velocity extends Module {

    private final SettingGroup sgTypes = addSg(new SettingGroup("tipos", true));

    public final NumberSetting xPercentage = sgGeneral.add(new NumberSetting(
            "X (%)",
            "porcentaje de retroceso en el eje X a tomar",
            0,
            0,
            100,
            1
    ));

    public final NumberSetting yPercentage = sgGeneral.add(new NumberSetting(
            "Y (%)",
            "porcentaje de retroceso vertical (Y) a tomar",
            0,
            0,
            100,
            1
    ));

    public final NumberSetting zPercentage = sgGeneral.add(new NumberSetting(
            "horizontal (%)",
            "porcentaje de retroceso en el eje Z a tomar",
            0,
            0,
            100,
            1
    ));

    private final BooleanSetting explosions = sgGeneral.add(new BooleanSetting(
            "explosiones",
            "tomar retroceso de explosiones",
            false
    ));

    public final BooleanSetting hits = sgTypes.add(new BooleanSetting(
            "hits",
            "tomar retroceso de golpes",
            false
    ));

    public final BooleanSetting push = sgTypes.add(new BooleanSetting(
            "empujar",
            "tomar retroceso de cuando te empuja una entidad",
            false
    ));

    public Velocity() {
        super("retroceso", "te hace estar gordo", Category.MOVEMENT);
    }

    @EventListener
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ExplosionS2CPacket explosionPacket && !explosions.getValue()) {
            explosionPacket.playerKnockback().ifPresent(kb -> ((ExplosionS2CPacketAccessor) (Object) explosionPacket).setKnockback(Optional.of(new Vec3d(
                    kb.x * xPercentage.getValue() / 100,
                    kb.y * yPercentage.getValue() / 100,
                    kb.z * zPercentage.getValue() / 100
            ))));
        } else if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket velocityPacket) {
            Vec3d kb = velocityPacket.getVelocity();
            ((EntityVelocityUpdateS2CPacketAccessor) velocityPacket).setVelocity(new Vec3d(
                    kb.x * xPercentage.getValue() / 100,
                    kb.y * yPercentage.getValue() / 100,
                    kb.z * zPercentage.getValue() / 100
            ));
        }
    }

}
