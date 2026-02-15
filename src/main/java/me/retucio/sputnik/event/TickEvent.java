package me.retucio.sputnik.event;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftClientMixin;


/**
 * @see MinecraftClientMixin#onTickPre
 * @see MinecraftClientMixin#onTickPost
 */
public class TickEvent {

    public static class Pre extends Event {}
    public static class Post extends Event {}
}
