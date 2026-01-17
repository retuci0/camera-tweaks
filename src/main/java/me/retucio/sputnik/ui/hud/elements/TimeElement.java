package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.TextHudElement;
import me.retucio.sputnik.util.MiscUtil;
import net.minecraft.text.Text;

import java.util.List;

public class TimeElement extends TextHudElement {

    public TimeElement() {
        super("time", mc.getWindow().getScaledWidth() - 50, mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 2);
    }

    @Override
    public String getText(float delta, HUD hud) {
        return MiscUtil.getCurrentFormattedTime();
    }

    @Override
    public String getPreviewText() {
        return MiscUtil.getCurrentFormattedTime();
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(
                Text.literal("hora"),
                Text.literal("hora y minutos de la zona horaria seleccionada")
        );
    }
}