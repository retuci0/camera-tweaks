package me.retucio.sputnik.event.network;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.hud.ChatComponentMixin;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;


/**
 * @see ChatComponentMixin#onAddMessage
 */

public class ReceiveMessageEvent extends Event {

    private Component text;
    private GuiMessageTag tag;
    private final int id;
    private boolean modified = false;

    public ReceiveMessageEvent(Component text, GuiMessageTag tag, int id) {
        this.text = text;
        this.tag =  tag;
        this.id = id;
    }

    public Component getMessage() {
        return text;
    }

    public GuiMessageTag getTag() {
        return tag;
    }

    public void setMessage(Component message) {
        this.text = message;
        this.modified = true;
    }

    public void setTag(GuiMessageTag tag) {
        this.tag = tag;
        this.modified = true;
    }

    public boolean wasModified() {
        return modified;
    }

    public int getId() {
        return id;
    }
}
