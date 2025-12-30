package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.JoinWorldEvent;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.DisconnectEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.NetworkUtil;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class PacketDelay extends Module {

    public NumberSetting delay = addSetting(new NumberSetting("delay", "delay a añadir a los paquetes, en milisegundos", 1500, 0, 3000, 5));
    public EnumSetting<Directions> directions = addSetting(new EnumSetting<>("dirección", "dirección de los paquetes a los que añadir delay",
            Directions.class, Directions.BOTH));
    public EnumSetting<Packets> packets = addSetting(new EnumSetting<>("paquetes", "paquetes a los que aplicar el delay",
            Packets.class, Packets.ALL));

    private final List<DelayedPacket> delayedPackets = new CopyOnWriteArrayList<>();
    private long lastProcessTime = 0;

    public PacketDelay() {
        super("delay de paquetes", "aplica un delay a los paquetes");
    }

    @Override
    public void onDisable() {
        for (DelayedPacket delayedPacket : delayedPackets)
            processDelayedPacket(delayedPacket);
        delayedPackets.clear();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) {
            delayedPackets.clear();
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastProcessTime < 50) return;
        lastProcessTime = currentTime;

        List<DelayedPacket> toProcess = new ArrayList<>();
        for (DelayedPacket packet : delayedPackets) {
            if (System.currentTimeMillis() >= packet.scheduledTime())
                toProcess.add(packet);
        }

        for (DelayedPacket packet : toProcess) {
            processDelayedPacket(packet);
            delayedPackets.remove(packet);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (directions.is(Directions.S2C)
                || mc.player == null || mc.world == null || mc.getNetworkHandler() == null
                || (event.getPacket() instanceof KeepAliveC2SPacket && !packets.is(Packets.OTHERS)))
            return;

        event.cancel();

        delayedPackets.add(new DelayedPacket(
                event.getPacket(),
                System.currentTimeMillis() + delay.getLongValue(),
                true,
                mc.getNetworkHandler().getConnection().getPacketListener()
        ));
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null
                || mc.getNetworkHandler() == null || mc.getNetworkHandler().getConnection() == null
                || (event.getPacket() instanceof KeepAliveS2CPacket && !packets.is(Packets.OTHERS)))
            return;

        event.cancel();

        ClientConnection connection = mc.getNetworkHandler().getConnection();

        delayedPackets.add(new DelayedPacket(
                event.getPacket(),
                System.currentTimeMillis() + delay.getLongValue(),
                false,
                connection.getPacketListener()
        ));
    }

    @SubscribeEvent
    public void onJoinWorld(JoinWorldEvent event) {
        ChatUtil.error("delay de paquetes no funciona en un solo jugador");
        toggle();
    }

    private void processDelayedPacket(DelayedPacket delayedPacket) {
        if (mc.getNetworkHandler() == null) return;

        if (delayedPacket.isOutgoing()) {
            NetworkUtil.sendPacketNoEvent(delayedPacket.packet());
        } else {
            if (delayedPacket.packetListener != null && mc.getNetworkHandler().getConnection() != null) {
                NetworkUtil.receivePacketNoEvent(delayedPacket.packet(), delayedPacket.packetListener());
            } else {
                NetworkUtil.receivePacketNoEvent(delayedPacket.packet());
            }
        }
    }

    private record DelayedPacket(Packet<?> packet, long scheduledTime, boolean isOutgoing, PacketListener packetListener) {}

    public enum Directions {
        C2S("cliente a server"),
        S2C("server a cliente"),
        BOTH("ambos");

        private final String name;
        Directions(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    public enum Packets {
        KEEP_ALIVE("paquetes KeepAlive"),
        OTHERS("todos menos KeepAlive"),
        ALL("todos");

        private final String name;
        Packets(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}