package me.retucio.camtweaks.mixin;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import me.retucio.camtweaks.event.Stage;
import me.retucio.camtweaks.event.events.DisconnectEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    // mandar paquetes

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacketPre(Packet<?> packet, ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        PacketEvent.Send event = EVENT_BUS.post(new PacketEvent.Send(packet, Stage.PRE));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("TAIL"), cancellable = true)
    private void onSendPacketPost(Packet<?> packet, ChannelFutureListener channelFutureListener, CallbackInfo ci) {
        PacketEvent.Send event = EVENT_BUS.post(new PacketEvent.Send(packet, Stage.POST));
        if (event.isCancelled()) ci.cancel();
    }


    // recibir paquetes

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        PacketEvent.Receive event = EVENT_BUS.post(new PacketEvent.Receive(packet));
        if (event.isCancelled()) ci.cancel();
    }


    // otros

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionInfo;)V", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(DisconnectionInfo disconnectionInfo, CallbackInfo ci) {
        DisconnectEvent event = EVENT_BUS.post(new DisconnectEvent());
        if (event.isCancelled()) ci.cancel();
    }
}