package me.retucio.sputnik.ui.hud;

import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.elements.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HudRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

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
        addElement(new PingElement(), positions, visibilities);
        addElement(new RotationElement(), positions, visibilities);
        addElement(new TimeElement(), positions, visibilities);
        addElement(new UptimeElement(), positions, visibilities);

        addElement(new DynoElement(), positions, visibilities);
        addElement(new EchestElement(), positions, visibilities);
        addElement(new TotemsElement(), positions, visibilities);

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

    public static HudElement getElement(Class<? extends HudElement> clazz) {
        for (HudElement element : elements) {
            if (element.getClass() == clazz)
                return element;
        }

        return null;
    }


    public static Color getColor(HUD hud) {
        return hud.color.getValue();
    }

    public static void drawSnappedText(GuiGraphicsExtractor gui, String text, int x, int y, int color, boolean shadow) {
        int textWidth = mc.font.width(text);
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        boolean snapRight = x > screenWidth / 2;
        int drawX = snapRight ? x + (textWidth + 2) - textWidth : x;

        drawX = Math.clamp(drawX, 0, screenWidth - textWidth);
        y = Math.clamp(y, 0, screenHeight - mc.font.lineHeight);

        gui.text(mc.font, text, drawX, y, color, shadow);
    }

    public static void render(GuiGraphicsExtractor gui, DeltaTracker dt) {
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);

        for (HudElement element : elements) {
            if (element.isVisible() && !shouldSkipRendering()) {
                element.renderInGame(gui, dt.getGameTimeDeltaTicks(), hud);
            }
        }
    }

    private static boolean shouldSkipRendering() {
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        return ModuleManager.INSTANCE == null
                || mc.player == null
                || mc.getCameraEntity() == null
                || mc.screen instanceof TitleScreen
                || mc.options.hideGui
                || !hud.isEnabled()
                || (mc.debugEntries.isOverlayVisible() && !hud.showOnF3.getValue())
                || (mc.screen instanceof ChatScreen && !hud.showOnChat.getValue())
                || mc.screen instanceof HudEditorScreen;
    }

    public static List<HudElement> getElements() {
        return elements;
    }
}