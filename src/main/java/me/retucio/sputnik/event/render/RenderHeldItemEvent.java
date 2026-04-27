package me.retucio.sputnik.event.render;

import com.github.retucio.neutrino.Event;
import com.mojang.blaze3d.vertex.PoseStack;
import me.retucio.sputnik.mixin.mixins.render.ItemInHandRendererMixin;
import net.minecraft.world.InteractionHand;


/**
 * @see ItemInHandRendererMixin#onRenderItem
 */

public class RenderHeldItemEvent extends Event {

    private PoseStack matrices;
    private InteractionHand hand;

    public RenderHeldItemEvent(PoseStack matrices, InteractionHand hand) {
        this.matrices = matrices;
        this.hand = hand;
    }

    public PoseStack getMatrices() {
        return matrices;
    }

    public void setMatrices(PoseStack matrices) {
        this.matrices = matrices;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }
}
