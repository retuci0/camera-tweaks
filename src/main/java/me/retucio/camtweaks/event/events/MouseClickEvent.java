package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;


/**
 * @see me.retucio.camtweaks.mixin.MouseMixin#onMouseButton
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
