package me.retucio.sputnik.ui.hud.elements;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.sputnik.UpdateSettingEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.TextHudElement;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CustomTextElement extends TextHudElement {

    public CustomTextElement() {
        super("customText", mc.getWindow().getGuiScaledWidth() / 2 - 40, 2);
    }

    @Override
    public String getText(float delta, HUD hud) {
        return hud != null && hud.customText != null ? hud.customText.getValue() : "";
    }

    @Override
    public String getPreviewText() {
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (hud != null && hud.customText != null && !hud.customText.getValue().isEmpty())
            return hud.customText.getValue();

        return "texto custom";
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.literal("texto custom"),
                Component.literal("texto que tú eliges")
        );
    }

    @EventListener
    public void onUpdateSetting(UpdateSettingEvent event) {
        if (ModuleManager.INSTANCE == null) return;
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (hud != null && event.getSetting() == hud.customText) {
            int newX = mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(hud.customText.getValue()) / 2;
            setPosition(newX, y);
        }
    }
}