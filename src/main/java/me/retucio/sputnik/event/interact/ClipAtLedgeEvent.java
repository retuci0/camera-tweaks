package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;

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
