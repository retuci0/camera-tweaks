package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

public class BoatMoveEvent extends Event {

    private final AbstractBoat boat;
    private Vec3 pos;

    public BoatMoveEvent(AbstractBoat boat, Vec3 pos) {
        this.boat = boat;
        this.pos = pos;
    }

    public AbstractBoat getBoat() {
        return boat;
    }

    public Vec3 getPos() {
        return pos;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }
}
