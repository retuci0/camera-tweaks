package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.network.chat.Component;

import java.util.List;


public class RotationElement extends TextHudElement {

    public RotationElement() {
        super("rotation",
                mc.getWindow().getGuiScaledWidth() - mc.font.width("180° 180° (N)"),
                mc.getWindow().getGuiScaledHeight() - mc.font.lineHeight
        );
    }

    @Override
    public String getText(float delta, Hud hud) {
        if (mc.player == null) return getPreviewText();
        return String.format("%.2f", mc.player.getYRot() % 360) + "° " + String.format("%.2f", mc.player.getXRot()) + "° (" + getDirection() + ")";
    }

    @Override
    public String getPreviewText() {
        if (mc.player == null)
            return "69° 69° (W)";
        return getText(mc.getDeltaTracker().getGameTimeDeltaTicks(), ModuleManager.INSTANCE.getModuleByClass(Hud.class));
    }

    @Override
    public List<Component> getTooltip() {
        return List.of();
    }

    private String getDirection() {
        return switch (mc.player.getDirection()) {
            case NORTH -> "N";
            case SOUTH -> "S";
            case EAST -> "E";
            case WEST -> "O";
            default -> "?";
        };
    }
}
