package me.retucio.sputnik.event.events.render;

import me.retucio.sputnik.event.Event;
import net.minecraft.client.option.Perspective;


/**
 * @see me.retucio.sputnik.mixin.GameOptionsMixin#changePerspective
 */
public class PerspectiveChangeEvent extends Event {

    private Perspective perspective;

    public PerspectiveChangeEvent(Perspective perspective) {
        this.perspective = perspective;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }
}
