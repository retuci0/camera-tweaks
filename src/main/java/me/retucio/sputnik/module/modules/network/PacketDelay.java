package me.retucio.sputnik.module.modules.network;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.JoinWorldEvent;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.NetworkUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class PacketDelay extends Module {

    private final NumberSetting delay = sgGeneral.add(new NumberSetting("delay", "delay a añadir a los paquetes, en milisegundos", 1500, 0, 3000, 5));
    private final EnumSetting<Directions> directions = sgGeneral.add(new EnumSetting<>("dirección", "dirección de los paquetes a los que añadir delay",
            Directions.class, Directions.BOTH));
    private final EnumSetting<Packets> packets = sgGeneral.add(new EnumSetting<>("paquetes", "paquetes a los que aplicar el delay",
            Packets.class, Packets.ALL));

    private final List<DelayedPacket> delayedPackets = new CopyOnWriteArrayList<>();
    private long lastProcessTime = 0;

    public PacketDelay() {
        super("delay de paquetes",
                "aplica un delay a los paquetes",
                Category.NETWORK);
    }

    @Override
    public void onEnable() {
        if (mc.isSingleplayer()) {
            ChatUtil.error("delay de paquetes no funciona en un solo jugador");
            toggle();
            return;
        }
        super.onEnable();
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
        if (mc.player == null || mc.level == null || mc.getConnection() == null) {
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

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (directions.is(Directions.S2C)
                || mc.player == null || mc.level == null || mc.getConnection() == null
                || (event.getPacket() instanceof ServerboundKeepAlivePacket && !packets.is(Packets.OTHERS)))
            return;

        event.cancel();

        delayedPackets.add(new DelayedPacket(
                event.getPacket(),
                System.currentTimeMillis() + delay.getLongValue(),
                true,
                mc.getConnection().getConnection().getPacketListener()
        ));
    }

    @EventListener
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.level == null
                || mc.getConnection() == null
                || directions.is(Directions.C2S)
                || (event.getPacket() instanceof ClientboundKeepAlivePacket
                && !packets.is(Packets.OTHERS))) {
            return;
        }

        event.cancel();

        Connection connection = mc.getConnection().getConnection();

        delayedPackets.add(new DelayedPacket(
                event.getPacket(),
                System.currentTimeMillis() + delay.getLongValue(),
                false,
                connection.getPacketListener()
        ));
    }

    @EventListener
    private void onJoinWorld(JoinWorldEvent event) {
        ChatUtil.error("delay de paquetes no funciona en un solo jugador");
        toggle();
    }

    private void processDelayedPacket(DelayedPacket delayedPacket) {
        if (mc.getConnection() == null) return;

        if (delayedPacket.isOutgoing()) {
            NetworkUtil.sendPacketNoEvent(delayedPacket.packet());
        } else {
            if (delayedPacket.packetListener != null) {
                NetworkUtil.receivePacketNoEvent(delayedPacket.packet(), delayedPacket.packetListener());
            } else {
                NetworkUtil.receivePacketNoEvent(delayedPacket.packet());
            }
        }
    }

    private record DelayedPacket(Packet<?> packet, long scheduledTime, boolean isOutgoing, PacketListener packetListener) {}

    private enum Directions {
        C2S("cliente a server"),
        S2C("server a cliente"),
        BOTH("ambos");

        private final String name;
        Directions(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    private enum Packets {
        KEEP_ALIVE("paquetes KeepAlive"),
        OTHERS("todos menos KeepAlive"),
        ALL("todos");

        private final String name;
        Packets(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}