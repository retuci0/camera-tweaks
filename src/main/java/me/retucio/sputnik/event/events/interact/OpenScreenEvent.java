package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;
import me.retucio.sputnik.mixin.mixins.misc.MinecraftClientMixin;
import net.minecraft.client.gui.screen.Screen;


/**
 * @see MinecraftClientMixin#onOpenScreen
 */
public class OpenScreenEvent extends Event {

    private final Screen screen;

    public OpenScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() {
        return screen;
    }
}