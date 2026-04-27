package me.retucio.sputnik.mixin.accessors;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(FishingHook.class)
public interface FishingHookAccessor {

    @Accessor("biting")
    boolean caughtFish();

    @Accessor("currentState")
    FishingHook.FishHookState getState();
}
