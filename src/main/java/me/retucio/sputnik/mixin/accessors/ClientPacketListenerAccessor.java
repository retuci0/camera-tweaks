package me.retucio.sputnik.mixin.accessors;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.SignedMessageChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {

    @Accessor("lastSeenMessages")
    LastSeenMessagesTracker getLastSeenMessagesTracker();

    @Accessor("signedMessageEncoder")
    SignedMessageChain.Encoder getSignedMessageEncoder();
}
