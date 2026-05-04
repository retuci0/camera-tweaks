package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.List;


public class PingElement extends TextHudElement {

    public PingElement() {
        super("ping", 2, 2 * (mc.font.lineHeight + 4));
    }

    @Override
    public String getText(float delta, Hud hud) {
        if (mc.getConnection() == null || mc.player == null) return "? ms";
        if (mc.isSingleplayer()) return "-1 ms";

        PlayerInfo playerListEntry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return playerListEntry != null ? playerListEntry.getLatency() + " ms" : "? ms";
    }

    @Override
    public String getPreviewText() {
        return "67 ms";
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.literal("ping"),
                Component.literal("latencia entre cliente y servidor, en milisegundos")
        );
    }
}