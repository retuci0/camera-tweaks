package me.retucio.camtweaks.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.event.events.ClientClickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "handleBasicClickEvent", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private static void handleClientClickEvents(ClickEvent clickEvent, MinecraftClient client, Screen screen, CallbackInfo ci) throws CommandSyntaxException {
        if (clickEvent instanceof ClientClickEvent event && event.getValue().startsWith(CommandManager.INSTANCE.getPrefix()))
            CommandManager.dispatch(event.getValue().substring(CommandManager.INSTANCE.getPrefix().length()));
    }
}
