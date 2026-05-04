package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.network.chat.Component;

import java.util.List;


public class FpsElement extends TextHudElement {

    public FpsElement() {
        super("fps", 2, 2);
    }

    @Override
    public String getText(float delta, Hud hud) {
        return "FPS: " + mc.getFps();
    }

    @Override
    public String getPreviewText() {
        return "FPS: " + mc.getFps();
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.literal("FPS"),
                Component.literal("te muestra los fotogramas por segundo a los que corre el juego")
        );
    }
}