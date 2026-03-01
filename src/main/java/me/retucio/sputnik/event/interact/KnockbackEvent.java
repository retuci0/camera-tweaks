package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
import net.minecraft.entity.LivingEntity;

public class KnockbackEvent extends Event {

    private double strength;
    private double x, z;
    private final LivingEntity target;

    public KnockbackEvent(LivingEntity target, double z, double x, double strength) {
        this.target = target;
        this.z = z;
        this.x = x;
        this.strength = strength;
    }

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public LivingEntity getTarget() {
        return target;
    }
}
