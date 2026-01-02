package me.retucio.camtweaks.ui.hud.elements;

import me.retucio.camtweaks.module.modules.client.HUD;
import me.retucio.camtweaks.ui.hud.HudElement;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.List;

public class PingElement extends HudElement {

    public PingElement() {
        super("ping", 2, 2 * (mc.textRenderer.fontHeight + 4));
    }

    @Override
    public String getText(float delta, HUD hud) {
        if (mc.getNetworkHandler() == null || mc.player == null) return "? ms";
        if (mc.isInSingleplayer()) return "-1 ms";

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return playerListEntry != null ? playerListEntry.getLatency() + " ms" : "? ms";
    }

    @Override
    public String getPreviewText() {
        return "67 ms";
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(
                Text.literal("ping"),
                Text.literal("latencia entre cliente y servidor, en milisegundos")
        );
    }
}