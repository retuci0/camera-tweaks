package me.retucio.camtweaks.mixin.accessor;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;

@Mixin(ClientCommonNetworkHandler.ConfirmServerResourcePackScreen.Pack.class)
public interface PackAccessor {

    @Invoker
    UUID callId();
}
