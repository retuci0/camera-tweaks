package me.retucio.sputnik.event.events.input;

import me.retucio.sputnik.event.Event;


/**
 * @see me.retucio.sputnik.mixin.KeyboardMixin#onKeyPress
 */
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
