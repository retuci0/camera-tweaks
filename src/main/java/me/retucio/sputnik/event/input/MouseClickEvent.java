package me.retucio.sputnik.event.input;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.io.MouseHandlerMixin;


/**
 * @see MouseHandlerMixin#onMouseButton
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
