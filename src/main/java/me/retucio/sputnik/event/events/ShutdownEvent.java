package me.retucio.sputnik.event.events;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftClientMixin;


/**
 * @see MinecraftClientMixin#onStop
 */
public class ShutdownEvent extends Event {

    public ShutdownEvent() {}
}
