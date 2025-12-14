package me.retucio.camtweaks.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.event.events.ClientClickEvent;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.retucio.camtweaks.CameraTweaks.getVersionName;
import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "handleBasicClickEvent", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private static void handleClientClickEvents(ClickEvent clickEvent, MinecraftClient client, Screen screen, CallbackInfo ci) throws CommandSyntaxException {
        if (clickEvent instanceof ClientClickEvent event && event.getValue().startsWith(CommandManager.INSTANCE.getPrefix()))
            CommandManager.dispatch(event.getValue().substring(CommandManager.INSTANCE.getPrefix().length()));
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void renderWatermark(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        String watermark = ClientSettingsFrame.guiSettings.watermark.getValue();
        if (watermark == null || watermark.isEmpty()) return;
        context.drawText(mc.textRenderer, watermark,
                mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(watermark) - 2, 2,
                new Color((int) ClientSettingsFrame.guiSettings.red.getDefaultValue(),
                        (int) ClientSettingsFrame.guiSettings.green.getDefaultValue(),
                        (int) ClientSettingsFrame.guiSettings.blue.getDefaultValue(),
                        (int) ClientSettingsFrame.guiSettings.alpha.getDefaultValue()
                ).getRGB(), false
        );
    }
}
