package me.retucio.sputnik.event.events.network;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.network.ClientPlayNetworkHandlerMixin;


/**
 * @see ClientPlayNetworkHandlerMixin#onSendMessage
 */
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
