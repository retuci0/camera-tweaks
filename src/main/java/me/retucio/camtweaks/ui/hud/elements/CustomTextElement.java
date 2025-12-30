package me.retucio.camtweaks.ui.hud.elements;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.camtweaks.UpdateSettingEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.HUD;
import me.retucio.camtweaks.ui.hud.HudElement;
import net.minecraft.text.Text;

import java.util.List;

public class CustomTextElement extends HudElement {

    public CustomTextElement() {
        super("customText", mc.getWindow().getScaledWidth() / 2 - 40, 2);
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
    public List<Text> getTooltip() {
        return List.of(
                Text.literal("texto custom"),
                Text.literal("texto que tú eliges")
        );
    }

    @SubscribeEvent
    public void onUpdateSetting(UpdateSettingEvent event) {
        if (ModuleManager.INSTANCE == null) return;
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (hud != null && event.getSetting() == hud.customText) {
            int newX = mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(hud.customText.getValue()) / 2;
            setPosition(newX, y);
        }
    }
}