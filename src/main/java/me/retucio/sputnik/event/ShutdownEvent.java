package me.retucio.sputnik.event;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftMixin;


/**
 * @see MinecraftMixin#onStop
 */
public class ShutdownEvent extends Event {

    public ShutdownEvent() {}
}
