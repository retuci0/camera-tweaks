package me.retucio.sputnik.event.interact;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.player.MultiPlayerGameModeMixin;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;


/**
 * @see MultiPlayerGameModeMixin#onBlockPlace
 */
public class PlaceBlockEvent extends Event {

    private InteractionHand hand;
    private BlockHitResult result;

    public PlaceBlockEvent(InteractionHand hand, BlockHitResult result) {
        this.hand = hand;
        this.result = result;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }

    public BlockHitResult getResult() {
        return result;
    }

    public void setResult(BlockHitResult result) {
        this.result = result;
    }
}
