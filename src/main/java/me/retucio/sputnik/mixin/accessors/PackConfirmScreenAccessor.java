package me.retucio.sputnik.mixin.accessors;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;


@Mixin(ClientCommonPacketListenerImpl.PackConfirmScreen.class)
public interface PackConfirmScreenAccessor {

    @Accessor("parentScreen")
    Screen getParentScreen();

    @Accessor("requests")
    List<? extends PendingRequestAccessor> getRequests();
}
