package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;
import net.minecraft.client.gui.screen.Screen;


/**
 * @see me.retucio.sputnik.mixin.MinecraftClientMixin#onOpenScreen
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