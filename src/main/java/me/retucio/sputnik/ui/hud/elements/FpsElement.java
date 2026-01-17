package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public class FpsElement extends TextHudElement {

    public FpsElement() {
        super("fps", 2, 2);
    }

    @Override
    public String getText(float delta, HUD hud) {
        return "FPS: " + mc.getCurrentFps();
    }

    @Override
    public String getPreviewText() {
        return "FPS: " + mc.getCurrentFps();
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(
                Text.literal("FPS"),
                Text.literal("te muestra los fotogramas por segundo a los que corre el juego")
        );
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        
    }
}