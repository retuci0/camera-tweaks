package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;

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
