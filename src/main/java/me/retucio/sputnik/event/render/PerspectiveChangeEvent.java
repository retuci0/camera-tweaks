package me.retucio.sputnik.event.render;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.GameOptionsMixin;
import net.minecraft.client.option.Perspective;


/**
 * @see GameOptionsMixin#changePerspective
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
