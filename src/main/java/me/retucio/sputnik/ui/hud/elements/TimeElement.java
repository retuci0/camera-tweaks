package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.hud.TextHudElement;
import me.retucio.sputnik.util.MiscUtil;
import net.minecraft.network.chat.Component;

import java.util.List;


public class TimeElement extends TextHudElement {

    public TimeElement() {
        super("time", mc.getWindow().getGuiScaledWidth() - 50, mc.getWindow().getGuiScaledHeight() - mc.font.lineHeight - 2);
    }

    @Override
    public String getText(float delta, Hud hud) {
        return MiscUtil.getCurrentFormattedTime();
    }

    @Override
    public String getPreviewText() {
        return MiscUtil.getCurrentFormattedTime();
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.literal("hora"),
                Component.literal("hora y minutos de la zona horaria seleccionada")
        );
    }
}