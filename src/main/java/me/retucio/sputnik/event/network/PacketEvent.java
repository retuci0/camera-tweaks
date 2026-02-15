package me.retucio.sputnik.event.network;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.network.ClientConnectionMixin;
import net.minecraft.network.packet.Packet;


/**
 * @see ClientConnectionMixin#onSendPacketPre
 * @see ClientConnectionMixin#onSendPacketPost
 * @see ClientConnectionMixin#onReceivePacket
 */
public class PacketEvent {

    public static class Send extends Event {

        private final Packet<?> packet;

        public Send(Packet<?> packet, Stage stage) {
            this.packet = packet;
            this.setStage(stage);
        }

        public Packet<?> getPacket() {
            return packet;
        }
    }

    public static class Receive extends Event {

        private final Packet<?> packet;

        public Receive(Packet<?> packet) {
            this.packet = packet;
        }

        public Packet<?> getPacket() {
            return packet;
        }
    }
}
