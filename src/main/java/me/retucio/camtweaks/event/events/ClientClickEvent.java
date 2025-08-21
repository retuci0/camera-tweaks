package me.retucio.camtweaks.event.events;

import net.minecraft.text.ClickEvent;

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
