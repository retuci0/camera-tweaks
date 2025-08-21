package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.client.option.Perspective;

// se genera en GameOptionsMixin
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
