package me.retucio.camtweaks.ui.hud;

import me.retucio.camtweaks.config.ConfigManager;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.HUD;
import me.retucio.camtweaks.ui.hud.elements.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.RenderTickCounter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HudRenderer {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final List<HudElement> elements = new ArrayList<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        Map<String, int[]> positions = ConfigManager.getConfig().hudPositions;
        Map<String, Boolean> visibilities = ConfigManager.getConfig().hudVisibilities;

        addElement(new CoordsElement(), positions, visibilities);
        addElement(new FpsElement(), positions, visibilities);
        addElement(new TpsElement(), positions, visibilities);
        addElement(new CustomTextElement(), positions, visibilities);
        addElement(new TimeElement(), positions, visibilities);
        addElement(new PingElement(), positions, visibilities);

        HudEditorScreen.INSTANCE.setElements(elements);
    }

    private static void addElement(HudElement element, Map<String, int[]> positions, Map<String, Boolean> visibilities) {
        int[] savedPos;

        if (positions.get(element.getId()) == null)
            savedPos = new int[] {element.defaultX, element.defaultY};
        else
            savedPos = positions.get(element.getId());

        Boolean savedVisibility = visibilities.get(element.getId());

        if (savedPos != null)
            element.setPosition(savedPos[0], savedPos[1]);

        if (savedVisibility != null)
            element.setVisible(savedVisibility);

        elements.add(element);
    }

    public static Color getColor(HUD hud) {
        return hud.color.getColor();
    }

    public static void drawSnappedText(DrawContext ctx, String text, int x, int y, int color, boolean shadow) {
        int textWidth = mc.textRenderer.getWidth(text);
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        boolean snapRight = x > screenWidth / 2;
        int drawX = snapRight ? x + (textWidth + 2) - textWidth : x;

        drawX = Math.max(0, Math.min(drawX, screenWidth - textWidth));
        y = Math.max(0, Math.min(y, screenHeight - mc.textRenderer.fontHeight));

        ctx.drawText(mc.textRenderer, text, drawX, y, color, shadow);
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (shouldSkipRendering()) return;

        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (!hud.isEnabled()) return;
        if (mc.debugHudEntryList.isF3Enabled() && !hud.showOnF3.isEnabled()) return;
        if (mc.currentScreen instanceof ChatScreen && !hud.showOnChat.isEnabled()) return;

        init();

        if (mc.currentScreen instanceof HudEditorScreen) return;

        float delta = tickCounter.getDynamicDeltaTicks();
        for (HudElement element : elements) {
            if (element.isVisible()) {
                element.renderInGame(ctx, delta, hud);
            }
        }
    }

    private static boolean shouldSkipRendering() {
        return ModuleManager.INSTANCE == null
                || mc.player == null
                || mc.getCameraEntity() == null
                || mc.currentScreen instanceof TitleScreen
                || mc.options.hudHidden;
    }

    public static List<HudElement> getElements() {
        return elements;
    }
}