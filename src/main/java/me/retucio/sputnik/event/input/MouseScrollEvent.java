package me.retucio.sputnik.event.input;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.io.MouseMixin;


/**
 * @see MouseMixin#onMouseScroll
 */
public class MouseScrollEvent extends Event {

    private double horizontal, vertical;

    public MouseScrollEvent(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public double getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(double horizontal) {
        this.horizontal = horizontal;
    }

    public double getVertical() {
        return vertical;
    }

    public void setVertical(double vertical) {
        this.vertical = vertical;
    }
}
