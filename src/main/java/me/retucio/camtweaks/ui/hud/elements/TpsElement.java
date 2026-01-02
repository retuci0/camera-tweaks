package me.retucio.camtweaks.ui.hud.elements;

import me.retucio.camtweaks.module.modules.client.HUD;
import me.retucio.camtweaks.ui.hud.HudElement;
import me.retucio.camtweaks.util.NetworkUtil;
import net.minecraft.text.Text;

import java.util.List;

public class TpsElement extends HudElement {

    public TpsElement() {
        super("tps", 2, mc.textRenderer.fontHeight + 4);
    }

    @Override
    public String getText(float delta, HUD hud) {
        return "TPS: " + NetworkUtil.getTPS();
    }

    @Override
    public String getPreviewText() {
        return "TPS: 20.0";
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(
                Text.literal("TPS"),
                Text.literal("te muestra la tasa de ticks por segundo del servidor (TPS óptimo: 20)")
        );
    }
}