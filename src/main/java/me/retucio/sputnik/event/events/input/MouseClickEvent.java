package me.retucio.sputnik.event.events.input;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.io.MouseMixin;


/**
 * @see MouseMixin#onMouseButton
 */
public class MouseClickEvent extends Event {

    private final int action, button;

    public MouseClickEvent(int action, int button) {
        this.action = action;
        this.button = button;
    }

    public int getAction() {
        return action;
    }

    public int getButton() {
        return button;
    }
}
