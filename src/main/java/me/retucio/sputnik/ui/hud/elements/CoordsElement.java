package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class CoordsElement extends TextHudElement {

    public CoordsElement() {
        super("coords", 2, mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight);
    }

    @Override
    public String getText(float delta, HUD hud) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        Vec3d pos = freecam != null && freecam.isEnabled()
                ? new Vec3d(freecam.getX(delta), freecam.getY(delta), freecam.getZ(delta))
                : mc.player == null
                    ? new Vec3d(67, 67, 67)
                    : mc.player.getEntityPos();

        String overworldCoords = (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
        String netherCoords = (int) pos.x / 8 + " " + (int) pos.y / 8 + " " + (int) pos.z / 8;

        if (hud.coordsMode.is(HUD.CoordsMode.OVERWORLD)) {
            return overworldCoords;
        } else if (hud.coordsMode.is(HUD.CoordsMode.NETHER)) {
            return netherCoords;
        } else {
            return overworldCoords + " (" + netherCoords + ")";
        }
    }

    @Override
    public String getPreviewText() {
        return getText(
                mc.getRenderTickCounter().getDynamicDeltaTicks(),
                ModuleManager.INSTANCE.getModuleByClass(HUD.class));
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(
                Text.literal(getId()),
                Text.literal("te muestra tu posición XYZ en el mundo")
        );
    }
}