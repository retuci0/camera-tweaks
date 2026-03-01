package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.text.Text;

import java.util.List;

public class UptimeElement extends TextHudElement {

    public UptimeElement() {
        super("uptime", mc.getWindow().getScaledWidth() - 50, mc.getWindow().getScaledHeight() - 2 * (mc.textRenderer.fontHeight + 2));
    }

    @Override
    public String getText(float delta, HUD hud) {
        long seconds = (System.currentTimeMillis() - Sputnik.LAUNCH_TIME) / 1000;
        int minutes = (int) seconds / 60;
        int hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;

        return String.format("%sh %smin %ss", hours, minutes, seconds);
    }

    @Override
    public String getPreviewText() {
        return getText(mc.getRenderTickCounter().getDynamicDeltaTicks(), ModuleManager.INSTANCE.getModuleByClass(HUD.class));
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(Text.of("tiempo jugando"));
    }
}
