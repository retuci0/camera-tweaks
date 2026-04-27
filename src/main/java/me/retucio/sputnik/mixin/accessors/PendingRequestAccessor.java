package me.retucio.sputnik.mixin.accessors;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;


@Mixin(ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest.class)
public interface PendingRequestAccessor {

    @Invoker
    UUID callId();
}
