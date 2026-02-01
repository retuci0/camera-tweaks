package me.retucio.sputnik.module.modules.network;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.StringSetting;
import me.retucio.sputnik.util.ChatUtil;

public class BungeecordSpoofer extends Module {

    public final StringSetting address = sgGeneral.add(new StringSetting("dirección", "la dirección IP que será enviada al server", "127.0.0.1", 15));

    public BungeecordSpoofer() {
        super("spoofer de bungeecord",
                "te permite conectarte a los servidores backend de un server bungeecord mal configurado",
                Category.NETWORK);

        address.onUpdate(text -> {
            if (!text.matches("^[0-9a-f\\\\.:]{0,45}$")) {
                address.setValue(address.getDefaultValue());
                ChatUtil.error("dirección IP inválida");
            }
        });
    }
}
