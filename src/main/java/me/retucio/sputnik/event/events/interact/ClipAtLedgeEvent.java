package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;

public class ClipAtLedgeEvent extends Event {

    private boolean clipping;

    public ClipAtLedgeEvent(boolean clipping) {
        this.clipping = clipping;
    }

    public boolean isClipping() {
        return clipping;
    }

    public void setClipping(boolean clipping) {
        this.clipping = clipping;
    }
}
