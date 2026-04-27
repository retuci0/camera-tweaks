package me.retucio.sputnik.mixin.mixins.network;

import com.github.retucio.neutrino.Event;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import me.retucio.sputnik.event.network.DisconnectEvent;
import me.retucio.sputnik.event.network.PacketEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;
import static me.retucio.sputnik.Sputnik.mc;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    // mandar paquetes

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacketPre(Packet<?> packet, @Nullable ChannelFutureListener listener, CallbackInfo ci) {
        PacketEvent.Send event = EVENT_BUS.post(new PacketEvent.Send(packet, Event.Stage.PRE));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("TAIL"), cancellable = true)
    private void onSendPacketPost(Packet<?> packet, ChannelFutureListener listener, CallbackInfo ci) {
        PacketEvent.Send event = EVENT_BUS.post(new PacketEvent.Send(packet, Event.Stage.POST));
        if (event.isCancelled()) ci.cancel();
    }


    // recibir paquetes

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        PacketEvent.Receive event = EVENT_BUS.post(new PacketEvent.Receive(packet));
        if (event.isCancelled()) ci.cancel();
    }


    // otros

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        DisconnectEvent event = EVENT_BUS.post(new DisconnectEvent(details, mc.getCurrentServer()));
        if (event.isCancelled()) ci.cancel();
    }
}