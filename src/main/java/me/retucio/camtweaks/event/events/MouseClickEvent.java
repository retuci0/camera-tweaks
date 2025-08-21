package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;

// se genera en MouseMixin
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
