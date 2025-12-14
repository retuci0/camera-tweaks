package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.StringSetting;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

public class ColoredSigns extends Module {

    public StringSetting symbol = addSetting(new StringSetting("símbolo", "símbolo a reemplazar por \"§\"", "&", 5));

    public ColoredSigns() {
        super("carteles gays", "te permite usar un símbolo a elegir como § para añadir formato a estos.");
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof UpdateSignC2SPacket packet) {
            for (int i = 0; i < 4; i++) {
                packet.getText()[i] = packet.getText()[i].replace(symbol.getValue(), "\247" + "\247a");
            }
        }
    }
}
