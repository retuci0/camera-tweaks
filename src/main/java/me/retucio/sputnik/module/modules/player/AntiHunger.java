package me.retucio.sputnik.module.modules.player;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {

    public AntiHunger() {
        super("anti hambre", "previene el hambre, haciéndole creer al server que nunca corres", Category.PLAYER);
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (mc.player.hasVehicle() || mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) return;

        if (event.getPacket() instanceof ClientCommandC2SPacket packet) {
            if (packet.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                event.cancel();
                if (mc.player.isGliding()) {
                    mc.player.stopGliding();
                }
            }
        }

        if (event.getPacket() instanceof PlayerMoveC2SPacket packet
                && packet.isOnGround()
                && mc.player.fallDistance <= 0
                && !mc.interactionManager.isBreakingBlock()) {
            event.cancel();
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getEntityPos(),
                    mc.player.getYaw(),
                    mc.player.getPitch(),
                    false,
                    mc.player.horizontalCollision
            ));
        }
    }
}
