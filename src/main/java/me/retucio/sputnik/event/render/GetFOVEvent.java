package me.retucio.sputnik.event.render;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.render.GameRendererMixin;


/**
 * @see GameRendererMixin#modifyFov
 */
public class GetFOVEvent extends Event {

    private float fov;

    public GetFOVEvent(float fov) {
        this.fov = fov;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }
}
