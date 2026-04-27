package me.retucio.sputnik.event.input;

import me.retucio.sputnik.module.modules.misc.ChatPlus;
import net.minecraft.network.chat.ClickEvent;
import org.jspecify.annotations.NonNull;


/** este evento es solo para asegurarse de que comandos del mod se puedan ejecutar solo desde el lado del cliente
 * @see ChatPlus#getSendButton
 */

public class ClientClickEvent implements ClickEvent {

    private final String value;

    public ClientClickEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public @NonNull Action action() {
        return Action.RUN_COMMAND;
    }

}
