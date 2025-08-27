package me.retucio.camtweaks.util.interfaces;

import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IVec3d {

    void smegma$set(double x, double y, double z);
    default void smegma$set(Vec3i vec) { smegma$set(vec.getX(), vec.getY(), vec.getZ()); }
    default void semgma$set(Vector3d vec) { smegma$set(vec.x, vec.y, vec.z); }

    void smegma$setXZ(double x, double z);
    void smegma$setY(double y);
}