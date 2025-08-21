package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;

public class SendMessageEvent extends Event {

    private String message;

    public SendMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
