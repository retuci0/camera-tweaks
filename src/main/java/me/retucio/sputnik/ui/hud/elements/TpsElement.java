package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.hud.TextHudElement;
import me.retucio.sputnik.util.NetworkUtil;
import net.minecraft.network.chat.Component;

import java.util.List;


public class TpsElement extends TextHudElement {

    public TpsElement() {
        super("tps", 2, mc.font.lineHeight + 4);
    }

    @Override
    public String getText(float delta, Hud hud) {
        return "TPS: " + NetworkUtil.getTPS();
    }

    @Override
    public String getPreviewText() {
        return "TPS: 20.0";
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.literal("TPS"),
                Component.literal("te muestra la tasa de ticks por segundo del servidor (TPS óptimo: 20)")
        );
    }
}