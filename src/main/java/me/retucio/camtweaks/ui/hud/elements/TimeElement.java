package me.retucio.camtweaks.ui.hud.elements;

import me.retucio.camtweaks.module.modules.client.HUD;
import me.retucio.camtweaks.ui.hud.HudElement;
import me.retucio.camtweaks.util.MiscUtil;
import net.minecraft.text.Text;

import java.util.List;

public class TimeElement extends HudElement {

    public TimeElement() {
        super("time", mc.getWindow().getScaledWidth() - 50, mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 2);
    }

    @Override
    public String getText(float delta, HUD hud) {
        return MiscUtil.getCurrentFormattedTime();
    }

    @Override
    public String getPreviewText() {
        return "04:20 PM";
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(
                Text.literal("hora"),
                Text.literal("hora y minutos de la zona horaria seleccionada")
        );
    }
}