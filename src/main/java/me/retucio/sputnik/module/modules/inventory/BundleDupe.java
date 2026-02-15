package me.retucio.sputnik.module.modules.inventory;

import com.github.retucio.neutrino.EventListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import me.retucio.sputnik.event.network.DisconnectEvent;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.Hand;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// versión simplificada del módulo de Numberz y Nummernuts, del mod de DupersUnited (https://discord.gg/dupes)
public class BundleDupe extends Module {

    private final NumberSetting timeoutSeconds = sgGeneral.add(new NumberSetting(
            "tiempo de espera",
            "tiempo a esperar en segundos antes de ser kickeado",
            30,
            1,
            120,
            1
    ));

    private final NumberSetting clickslotPackets = sgGeneral.add(new NumberSetting(
            "paquetes",
            "paquetes a enviar",
            200,
            1,
            1000,
            1
    ));

    private boolean cancelKeepAlive = false;
    private boolean dupeActivated = false;
    private boolean waitingForKeepAlive = false;

    private final ScheduledExecutorService delayedExecutor = new ScheduledThreadPoolExecutor(2);

    public BundleDupe() {
        super("dupe de bundle",
                "dupea items usando un bundle y un libro",
                Category.INVENTORY
        );
    }

    @Override
    public void onEnable() {
        dupeActivated = true;
        dupe();
    }

    @Override
    public void onDisable() {
        this.cancelKeepAlive = false;
        this.dupeActivated = false;
        this.waitingForKeepAlive = false;
    }

    @EventListener
    private void onDisconnect(DisconnectEvent event) {
        this.toggle();
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (this.dupeActivated && event.getPacket() instanceof PlayerActionC2SPacket) {
            event.cancel();
        }

        if (event.getPacket() instanceof KeepAliveC2SPacket) {
            if (waitingForKeepAlive) {
                waitingForKeepAlive = false;
                cancelKeepAlive = true;
                ChatUtil.info("KeepAlive enviado, esperando " + this.timeoutSeconds.getValue() + " segundos...");
                delayedExecutor.schedule(() -> {
                    if (isEnabled()) {
                        sendInteractItem();
                        sendClickSlots();
                        toggle();
                    }
                }, timeoutSeconds.getIntValue() * 1000L, TimeUnit.MILLISECONDS);
            } else if (cancelKeepAlive) {
                event.cancel();
            }
        }
    }

    private void dupe() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        sendClickSlots();
        sendInteractItem();
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(mc.player, false));

        sendClickSlots();
        delayedExecutor.schedule(() -> {
            if (isEnabled()) {
                this.toggle();
            }
        }, 100L, TimeUnit.MILLISECONDS);
    }

    private void sendInteractItem() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        PlayerInteractItemC2SPacket packet = new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), this.mc.player.getPitch());
        mc.getNetworkHandler().sendPacket(packet);
    }

    private void sendClickSlots() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        for (int i = 0; i < this.clickslotPackets.getIntValue(); i++) {
            ClickSlotC2SPacket packet = new ClickSlotC2SPacket(0, 0, (short) 0, (byte) 0, SlotActionType.PICKUP, new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY);
            this.mc.getNetworkHandler().sendPacket(packet);
        }

        ChatUtil.info(clickslotPackets.getIntValue() + " paquetes ClickSlot enviados");
    }
}
