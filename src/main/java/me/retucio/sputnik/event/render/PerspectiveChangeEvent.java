package me.retucio.sputnik.event.render;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.misc.OptionsMixin;
import net.minecraft.client.CameraType;


/**
 * @see OptionsMixin#changePerspective
 */
public class PerspectiveChangeEvent extends Event {

    private CameraType perspective;

    public PerspectiveChangeEvent(CameraType perspective) {
        this.perspective = perspective;
    }

    public CameraType getPerspective() {
        return perspective;
    }

    public void setPerspective(CameraType perspective) {
        this.perspective = perspective;
    }
}
