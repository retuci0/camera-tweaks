package me.retucio.sputnik.module.modules.inventory;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;


public class XCarry extends Module {

    private boolean invOpen;

    public XCarry() {
        super("mochila", "te permite llevar items en los slots de crafteo", Category.INVENTORY);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.connection.send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
        }
        invOpen = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (invOpen && mc.player != null) {
            mc.player.connection.send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
        }
        super.onDisable();
    }


    @EventListener
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundContainerClosePacket packet
                && packet.getContainerId() == mc.player.inventoryMenu.containerId) {
            invOpen = true;
            event.cancel();
        }
    }
}
