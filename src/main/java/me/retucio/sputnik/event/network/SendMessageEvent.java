package me.retucio.sputnik.event.network;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.network.ClientPacketListenerMixin;


/**
 * @see ClientPacketListenerMixin#onSendMessage
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
