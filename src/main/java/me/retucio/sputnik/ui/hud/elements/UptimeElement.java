package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.network.chat.Component;

import java.util.List;


public class UptimeElement extends TextHudElement {

    public UptimeElement() {
        super("uptime", mc.getWindow().getGuiScaledWidth() - 50, mc.getWindow().getGuiScaledHeight() - 2 * (mc.font.lineHeight + 2));
    }

    @Override
    public String getText(float delta, Hud hud) {
        long seconds = (System.currentTimeMillis() - Sputnik.LAUNCH_TIME) / 1000;
        int minutes = (int) seconds / 60;
        int hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;

        return String.format("%sh %smin %ss", hours, minutes, seconds);
    }

    @Override
    public String getPreviewText() {
        return getText(mc.getDeltaTracker().getGameTimeDeltaTicks(), ModuleManager.INSTANCE.getModuleByClass(Hud.class));
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(Component.literal("tiempo jugando"));
    }
}
