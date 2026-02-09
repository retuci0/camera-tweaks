package me.retucio.sputnik.event.events;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftClientMixin;


/**
 * @see MinecraftClientMixin#onTickPre
 * @see MinecraftClientMixin#onTickPost
 */
public class TickEvent {

    public static class Pre extends Event {}
    public static class Post extends Event {}
}
