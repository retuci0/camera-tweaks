package me.retucio.camtweaks.event.events;

import io.netty.channel.ChannelFutureListener;
import me.retucio.camtweaks.event.Event;
import me.retucio.camtweaks.event.Stage;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * @see me.retucio.camtweaks.mixin.ClientConnectionMixin#onSendPacketPre
 * @see me.retucio.camtweaks.mixin.ClientConnectionMixin#onSendPacketPost 
 * @see me.retucio.camtweaks.mixin.ClientConnectionMixin#onReceivePacket
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
