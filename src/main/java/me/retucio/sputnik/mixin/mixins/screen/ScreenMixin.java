package me.retucio.sputnik.mixin.mixins.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.event.input.ClientClickEvent;
import me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.sputnik.Sputnik.mc;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "defaultHandleClickEvent", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private static void handleClientClickEvents(ClickEvent event, Minecraft minecraft, Screen activeScreen, CallbackInfo ci) throws CommandSyntaxException {
        if (event instanceof ClientClickEvent e && e.getValue().startsWith(CommandManager.INSTANCE.getPrefix())) {
            CommandManager.dispatch(e.getValue().substring(CommandManager.INSTANCE.getPrefix().length()));
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void renderWatermark(GuiGraphicsExtractor gui, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!ConfigManager.INSTANCE.hasLoaded()) return;  // para evitar dibujar la marca de agua por defecto
        String watermark = ClientSettingsPanel.clientSettings.watermark.getValue();
        if (watermark == null || watermark.isEmpty()) return;
        gui.text(mc.font, watermark,
                mc.getWindow().getGuiScaledWidth() - mc.font.width(watermark) - 2, 2,
                ClientSettingsPanel.clientSettings.color.getRGB(), false
        );
    }
}
