package me.retucio.sputnik.module.modules.player;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;


public class AntiHunger extends Module {

    public AntiHunger() {
        super("anti hambre", "previene el hambre, haciéndole creer al server que nunca corres", Category.PLAYER);
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (mc.player.isPassenger() || mc.player.isInWater() || mc.player.isUnderWater()) return;

        if (event.getPacket() instanceof ServerboundPlayerCommandPacket packet) {
            if (packet.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
                event.cancel();
                if (mc.player.isFallFlying()) {
                    mc.player.stopFallFlying();
                }
            }
        }

        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet
                && packet.isOnGround()
                && mc.player.fallDistance <= 0
                && !mc.gameMode.isDestroying()) {
            event.cancel();
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                    mc.player.position(),
                    mc.player.getYRot(),
                    mc.player.getXRot(),
                    false,
                    mc.player.horizontalCollision
            ));
        }
    }
}
