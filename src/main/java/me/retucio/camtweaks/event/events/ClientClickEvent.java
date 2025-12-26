package me.retucio.camtweaks.event.events;

import net.minecraft.text.ClickEvent;


/** este evento es solo para asegurarse de que comandos del mod se puedan ejecutar solo desde el lado del cliente
 * @see me.retucio.camtweaks.module.modules.ChatPlus#getSendButton
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
    public Action getAction() {
        return Action.RUN_COMMAND;
    }

}
