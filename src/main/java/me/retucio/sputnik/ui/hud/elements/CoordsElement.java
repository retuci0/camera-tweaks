package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class CoordsElement extends TextHudElement {

    public CoordsElement() {
        super("coords", 2, mc.getWindow().getGuiScaledHeight() - mc.font.lineHeight);
    }

    @Override
    public String getText(float delta, HUD hud) {
        Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        Vec3 pos = freecam != null && freecam.isEnabled()
                ? new Vec3(freecam.getX(delta), freecam.getY(delta), freecam.getZ(delta))
                : mc.player == null
                    ? new Vec3(67, 67, 67)
                    : mc.player.position();

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
                mc.getDeltaTracker().getGameTimeDeltaTicks(),
                ModuleManager.INSTANCE.getModuleByClass(HUD.class));
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.literal(getId()),
                Component.literal("te muestra tu posición XYZ en el mundo")
        );
    }
}