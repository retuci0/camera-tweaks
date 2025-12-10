package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    // marca de agua porque soy superguay
    @Inject(method = "render", at = @At("TAIL"))
    private void renderWatermark(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        String watermark = CameraTweaks.MOD_ID + "_v" + CameraTweaks.MOD_VERSION + "_" + SharedConstants.getGameVersion().name();
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
