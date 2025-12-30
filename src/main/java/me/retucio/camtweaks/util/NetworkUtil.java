package me.retucio.camtweaks.util;


import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class NetworkUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // tps
    private static final List<Float> tpsHistory = new ArrayList<>();
    private static float estimatedTPS = 20f;
    private static long lastWorldTime = -1L;
    private static long lastRealTime = -1L;

    public static float getTPS() {
        return estimatedTPS;
    }

    public static void updateTPS(float tps) {
        tpsHistory.add(tps);
        if (tpsHistory.size() > 10)
            tpsHistory.removeFirst();


        float sum = 0;
        for (float t : tpsHistory) sum += t;
        estimatedTPS = sum / tpsHistory.size();
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket packet) {
            long currentWorldTime = packet.time();
            long currentRealTime = System.currentTimeMillis();

            if (lastWorldTime != -1L && lastRealTime != -1L) {
                long elapsedRealTime = currentRealTime - lastRealTime;
                long elapsedWorldTicks = currentWorldTime - lastWorldTime;

                if (elapsedRealTime > 0) {
                    float tps = (float) elapsedWorldTicks / (elapsedRealTime / 1000.0f);
                    tps = Math.max(0.1f, Math.min(20.0f, tps));
                    updateTPS(tps);
                }
            }

            lastWorldTime = currentWorldTime;
            lastRealTime = currentRealTime;
        }
    }


    public static void sendPacketNoEvent(Packet<?> packet) {
        mc.getNetworkHandler().getConnection().sendImmediately(packet, null, true);
    }

    public static void receivePacketNoEvent(Packet<?> packet) {
        receivePacketNoEvent(packet, mc.getNetworkHandler().getConnection().getPacketListener());
    }

    public static void receivePacketNoEvent(Packet<?> packet, PacketListener listener) {
        ClientConnection.handlePacket(packet, listener);
    }
}