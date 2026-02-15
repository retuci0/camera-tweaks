package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
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