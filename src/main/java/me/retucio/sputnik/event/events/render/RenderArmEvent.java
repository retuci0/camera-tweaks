package me.retucio.sputnik.event.events.render;

import me.retucio.sputnik.event.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;


/**
 * @see me.retucio.sputnik.mixin.HeldItemRendererMixin#onRenderArm
 */
public class RenderArmEvent extends Event {

    private MatrixStack matrices;
    private Hand hand;

    public RenderArmEvent(MatrixStack matrices, Hand hand) {
        this.matrices = matrices;
        this.hand = hand;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public void setMatrices(MatrixStack matrices) {
        this.matrices = matrices;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }
}
