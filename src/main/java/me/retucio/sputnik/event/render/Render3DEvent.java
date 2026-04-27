package me.retucio.sputnik.event.render;

import com.github.retucio.neutrino.Event;
import com.mojang.blaze3d.vertex.PoseStack;
import me.retucio.sputnik.mixin.mixins.render.LevelRendererMixin;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;


/**
 * @see LevelRendererMixin#renderLevel
 */

public class Render3DEvent extends Event {

    private final PoseStack matrices;
    private final DeltaTracker dt;
    private final CameraRenderState cameraState;

    public Render3DEvent(PoseStack matrices, DeltaTracker dt, CameraRenderState cameraState) {
        this.matrices = matrices;
        this.dt = dt;
        this.cameraState = cameraState;
    }

    public PoseStack getMatrices() {
        return matrices;
    }

    public DeltaTracker getDeltaTracker() {
        return dt;
    }

    public CameraRenderState getCamera() {
        return cameraState;
    }

    public float getTickDelta() {
        return dt.getGameTimeDeltaTicks();
    }
}
