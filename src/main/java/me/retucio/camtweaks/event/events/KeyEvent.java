package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;

// se genera en KeyboardMixin
public class KeyEvent extends Event {

    private final int key, scancode, action;

    public KeyEvent(int key, int scancode, int action) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
    }

    public int getKey() {
        return key;
    }

    public int getAction() {
        return action;
    }

    public int getScancode() {
        return scancode;
    }
}
