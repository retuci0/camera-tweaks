package me.retucio.sputnik.event.events.network;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.event.Stage;
import net.minecraft.network.packet.Packet;


/**
 * @see me.retucio.sputnik.mixin.ClientConnectionMixin#onSendPacketPre
 * @see me.retucio.sputnik.mixin.ClientConnectionMixin#onSendPacketPost
 * @see me.retucio.sputnik.mixin.ClientConnectionMixin#onReceivePacket
 */
public class PacketEvent {

    public static class Send extends Event {

        private final Packet<?> packet;
        private final Stage stage;

        public Send(Packet<?> packet, Stage stage) {
            this.packet = packet;
            this.stage = stage;
        }

        public Packet<?> getPacket() {
            return packet;
        }

        public Stage getStage() {
            return stage;
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
