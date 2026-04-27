package me.retucio.sputnik.mixin.mixins.misc;

import me.retucio.sputnik.util.interfaces.IVec3;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(Vec3.class)
public abstract class Vec3Mixin implements IVec3 {

    @Shadow @Final @Mutable
    public double x;

    @Shadow @Final @Mutable
    public double y;

    @Shadow @Final @Mutable
    public double z;

    @Override
    public void sputnik$set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void sputnik$setXZ(double x, double z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public void sputnik$setY(double y) {
        this.y = y;
    }
}
