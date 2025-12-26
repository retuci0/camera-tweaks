package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;

/**
 * @see me.retucio.camtweaks.mixin.ChatHudMixin#onAddMessage
 */
public class ReceiveMessageEvent extends Event {

    private Text text;
    private MessageIndicator indicator;
    private int id;
    private boolean modified = false;

    public ReceiveMessageEvent(Text text, MessageIndicator indicator, int id) {
        this.text = text;
        this.indicator =  indicator;
        this.id = id;
    }

    public Text getMessage() {
        return text;
    }

    public MessageIndicator getIndicator() {
        return indicator;
    }

    public void setMessage(Text message) {
        this.text = message;
        this.modified = true;
    }

    public void setIndicator(MessageIndicator indicator) {
        this.indicator = indicator;
        this.modified = true;
    }

    public boolean wasModified() {
        return modified;
    }


}
