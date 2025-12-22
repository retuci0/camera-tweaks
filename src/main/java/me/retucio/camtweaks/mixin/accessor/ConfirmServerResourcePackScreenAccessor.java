package me.retucio.camtweaks.mixin.accessor;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientCommonNetworkHandler.ConfirmServerResourcePackScreen.class)
public interface ConfirmServerResourcePackScreenAccessor {

    @Accessor("parent")
    Screen getParentScreen();

    @Accessor("packs")
    List<? extends PackAccessor> getRequests();
}
