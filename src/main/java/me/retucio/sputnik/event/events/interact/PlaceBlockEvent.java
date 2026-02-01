package me.retucio.sputnik.event.events.interact;

import me.retucio.sputnik.event.Event;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;


/**
 * @see me.retucio.sputnik.mixin.ClientPlayerInteractionManagerMixin#onBlockPlace
 */
public class PlaceBlockEvent extends Event {

    private Hand hand;
    private BlockHitResult result;

    public PlaceBlockEvent(Hand hand, BlockHitResult result) {
        this.hand = hand;
        this.result = result;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public BlockHitResult getResult() {
        return result;
    }

    public void setResult(BlockHitResult result) {
        this.result = result;
    }
}
