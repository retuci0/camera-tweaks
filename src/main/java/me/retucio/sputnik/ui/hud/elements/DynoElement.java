package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.ImageHudElement;
import net.minecraft.network.chat.Component;

import java.util.List;


public class DynoElement extends ImageHudElement {

    private HUD.Dynosaurs dyno;

    public DynoElement() {
        super("dyno",
                mc.getWindow().getGuiScaledWidth() - 100,
                mc.getWindow().getGuiScaledHeight() - 100);
        reloadTexture();
    }

    @Override
    protected String getImagePath() {
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (hud == null) return "textures/dynos/spinosaurus.png";

        dyno = hud.dyno.getValue();
        return "textures/dynos/" + getDynoFileName(dyno) + ".png";
    }

    private String getDynoFileName(HUD.Dynosaurs dyno) {
        return dyno.toRealString().toLowerCase();
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.literal("dinosaurio: " + dyno),
                Component.literal("que viva el autismo joder")
        );
    }
}
