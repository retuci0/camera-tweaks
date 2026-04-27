package me.retucio.sputnik.module.modules.world;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.StringSetting;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;


public class ColoredSigns extends Module {

    private final StringSetting symbol = sgGeneral.add(new StringSetting(
            "símbolo",
            "símbolo a reemplazar por \"§\"",
            "&",
            5
    ));

    public ColoredSigns() {
        super("carteles gays",
                "te permite usar un símbolo a elegir como § para añadir formato a estos.",
                Category.WORLD);
    }

    @EventListener
    private void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof ServerboundSignUpdatePacket packet) {
            for (int i = 0; i < 4; i++) {
                packet.getLines()[i] = packet.getLines()[i].replace(symbol.getValue(), "\247" + "\247a");
            }
        }
    }
}
