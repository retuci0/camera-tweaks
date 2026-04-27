package me.retucio.sputnik.module.modules.inventory;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.DisconnectEvent;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;

import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;


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
        if (this.dupeActivated && event.getPacket() instanceof ServerboundPlayerActionPacket) {
            event.cancel();
        }

        if (event.getPacket() instanceof ServerboundKeepAlivePacket) {
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
        if (mc.player == null || mc.getConnection() == null) return;

        sendClickSlots();
        sendInteractItem();
            mc.getConnection().send(new ServerboundAttackPacket(mc.player.getId()));

        sendClickSlots();
        delayedExecutor.schedule(() -> {
            if (isEnabled()) {
                this.toggle();
            }
        }, 100L, TimeUnit.MILLISECONDS);
    }

    private void sendInteractItem() {
        if (mc.player == null || mc.getConnection() == null) return;

        ServerboundUseItemPacket packet = new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, mc.player.getYRot(), this.mc.player.getXRot());
        mc.getConnection().send(packet);
    }

    private void sendClickSlots() {
        if (mc.player == null || mc.getConnection() == null) return;

        for (int i = 0; i < this.clickslotPackets.getIntValue(); i++) {
            ServerboundContainerClickPacket packet = new ServerboundContainerClickPacket(
                    0, 0, (short) 0, (byte) 0,
                    ContainerInput.PICKUP,
                    new Int2ObjectArrayMap<>(),
                    HashedStack.EMPTY
            );
            this.mc.getConnection().send(packet);
        }

        ChatUtil.info(clickslotPackets.getIntValue() + " paquetes ClickSlot enviados");
    }
}
