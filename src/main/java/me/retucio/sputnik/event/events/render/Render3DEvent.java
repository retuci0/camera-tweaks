package me.retucio.sputnik.event.events.render;

import me.retucio.sputnik.event.Event;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;


/**
 * @see me.retucio.sputnik.mixin.WorldRendererMixin#render
 */
public class Render3DEvent extends Event {

    private final MatrixStack matrices;
    private final RenderTickCounter tc;
    private final Camera camera;

    public Render3DEvent(MatrixStack matrices, RenderTickCounter tc, Camera camera) {
        this.matrices = matrices;
        this.tc = tc;
        this.camera = camera;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public RenderTickCounter getTickCounter() {
        return tc;
    }

    public Camera getCamera() {
        return camera;
    }
}
