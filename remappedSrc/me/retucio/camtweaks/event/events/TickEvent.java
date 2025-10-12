package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;

/**
 * @see me.retucio.camtweaks.mixin.MinecraftClientMixin#onTickPre
 * @see me.retucio.camtweaks.mixin.MinecraftClientMixin#onTickPost
 */

public class TickEvent {

    public static class Pre extends Event {}
    public static class Post extends Event {}
}
