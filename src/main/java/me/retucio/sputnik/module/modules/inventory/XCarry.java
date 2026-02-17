package me.retucio.sputnik.module.modules.inventory;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class XCarry extends Module {

    private boolean invOpen;

    public XCarry() {
        super("mochila", "te permite llevar items en los slots de crafteo", Category.INVENTORY);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
        }
        invOpen = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (invOpen && mc.player != null) {
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
        }
        super.onDisable();
    }


    @EventListener
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket packet
                && packet.getSyncId() == mc.player.playerScreenHandler.syncId) {
            invOpen = true;
            event.cancel();
        }
    }
}
