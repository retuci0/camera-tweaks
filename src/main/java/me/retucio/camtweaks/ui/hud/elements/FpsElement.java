package me.retucio.camtweaks.ui.hud.elements;

import me.retucio.camtweaks.module.modules.client.HUD;
import me.retucio.camtweaks.ui.hud.HudElement;
import net.minecraft.text.Text;

import java.util.List;

public class FpsElement extends HudElement {

    public FpsElement() {
        super("fps", 2, 2);
    }

    @Override
    public String getText(float delta, HUD hud) {
        return "FPS: " + mc.getCurrentFps();
    }

    @Override
    public String getPreviewText() {
        return "FPS: 67";
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(
                Text.literal("FPS"),
                Text.literal("te muestra los fotogramas por segundo a los que corre el juego")
        );
    }
}