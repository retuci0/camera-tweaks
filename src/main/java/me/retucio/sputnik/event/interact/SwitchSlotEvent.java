package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;

public class SwitchSlotEvent extends Event {

    private final int prevSlot;
    private int slot;

    public SwitchSlotEvent(int prevSlot, int slot) {
        this.prevSlot = prevSlot;
        this.slot = slot;
    }

    public int getPrevSlot() {
        return prevSlot;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }
}
