package me.retucio.sputnik.event.render;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.render.HeldItemRendererMixin;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;


/**
 * @see HeldItemRendererMixin#onRenderItem
 */
public class RenderHeldItemEvent extends Event {

    private MatrixStack matrices;
    private Hand hand;

    public RenderHeldItemEvent(MatrixStack matrices, Hand hand) {
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
