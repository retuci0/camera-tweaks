package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.client.gui.screen.Screen;


/**
 * @see me.retucio.camtweaks.mixin.MinecraftClientMixin#onOpenScreen
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