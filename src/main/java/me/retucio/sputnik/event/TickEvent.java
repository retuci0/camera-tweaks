package me.retucio.sputnik.event;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftMixin;


/**
 * @see MinecraftMixin#onTickPre
 * @see MinecraftMixin#onTickPost
 */
public class TickEvent {

    public static class Pre extends Event {}
    public static class Post extends Event {}
}
