package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.text.Text;

import java.util.List;

public class RotationElement extends TextHudElement {

    public RotationElement() {
        super("rotation", mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth("180º 180º (N)"), mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight);
    }

    @Override
    public String getText(float delta, HUD hud) {
        if (mc.player == null) return getPreviewText();
        return String.format("%.2f", mc.player.getYaw() % 360) + "º " + String.format("%.2f", mc.player.getPitch()) + "º (" + getDirection() + ")";
    }

    @Override
    public String getPreviewText() {
        if (mc.player == null)
            return "69º 69º (W)";
        return getText(mc.getRenderTickCounter().getDynamicDeltaTicks(), ModuleManager.INSTANCE.getModuleByClass(HUD.class));
    }

    @Override
    public List<Text> getTooltip() {
        return List.of();
    }

    private String getDirection() {
        return switch (mc.player.getHorizontalFacing()) {
            case NORTH -> "N";
            case SOUTH -> "S";
            case EAST -> "E";
            case WEST -> "O";
            default -> "?";
        };
    }
}
