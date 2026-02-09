package me.retucio.sputnik.event.events.render;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.entity.EntityMixin;


/**
 * @see EntityMixin#onRotation
 * @see EntityMixin#onChangeYaw
 * @see EntityMixin#onChangePitch
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
