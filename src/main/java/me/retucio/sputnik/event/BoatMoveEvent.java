package me.retucio.sputnik.event;

import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Vec3d;

public class BoatMoveEvent extends Event {

    private final AbstractBoatEntity boat;
    private Vec3d pos;

    public BoatMoveEvent(AbstractBoatEntity boat, Vec3d pos) {
        this.boat = boat;
        this.pos = pos;
    }

    public AbstractBoatEntity getBoat() {
        return boat;
    }

    public Vec3d getPos() {
        return pos;
    }

    public void setPos(Vec3d pos) {
        this.pos = pos;
    }
}
