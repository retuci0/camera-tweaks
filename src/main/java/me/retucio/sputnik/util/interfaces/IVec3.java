package me.retucio.sputnik.util.interfaces;

import net.minecraft.core.Vec3i;
import org.joml.Vector3d;


public interface IVec3 {

    void sputnik$set(double x, double y, double z);
    default void sputnik$set(Vec3i vec) { sputnik$set(vec.getX(), vec.getY(), vec.getZ()); }
    default void sputnik$set(Vector3d vec) { sputnik$set(vec.x, vec.y, vec.z); }

    void sputnik$setXZ(double x, double z);
    void sputnik$setY(double y);
}