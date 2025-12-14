package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.StringSetting;
import me.retucio.camtweaks.util.ChatUtil;

public class BungeecordSpoofer extends Module {

    public StringSetting address = addSetting(new StringSetting("dirección", "la dirección IP que será enviada al server", "127.0.0.1", 15));

    public BungeecordSpoofer() {
        super("spoofer de bungeecord", "te permite conectarte a los servidores backend de un server bungeecord mal configurado");

        address.onUpdate(text -> {
            if (!text.matches("^[0-9a-f\\\\.:]{0,45}$")) {
                address.setValue(address.getDefaultValue());
                ChatUtil.error("dirección IP inválida");
            }
        });
    }
}
