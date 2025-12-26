package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;


/**
 * @see me.retucio.camtweaks.mixin.EntityMixin#onRotation
 * @see me.retucio.camtweaks.mixin.EntityMixin#onChangeYaw
 * @see me.retucio.camtweaks.mixin.EntityMixin#onChangePitch
 */
public class ChangeRotationEvent extends Event {

    private float yaw;
    private float pitch;

    public ChangeRotationEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
