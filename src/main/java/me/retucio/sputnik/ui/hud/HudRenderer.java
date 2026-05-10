package me.retucio.sputnik.ui.hud;

import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.hud.elements.*;
import me.retucio.sputnik.ui.widgets.misc.NotificationWidget;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.TitleScreen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class HudRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

    public static HudRenderer INSTANCE;

    private final List<HudElement> elements = new ArrayList<>();
    private final Stack<NotificationWidget> notifications = new Stack<>();
    private boolean initialized = false;

    public void init() {
        if (initialized) return;
        initialized = true;

        Map<String, int[]> positions = ConfigManager.INSTANCE.getConfig().hudPositions;
        Map<String, Boolean> visibilities = ConfigManager.INSTANCE.getConfig().hudVisibilities;

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

    private void addElement(HudElement element, Map<String, int[]> positions, Map<String, Boolean> visibilities) {
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

    public HudElement getElement(Class<? extends HudElement> clazz) {
        for (HudElement element : elements) {
            if (element.getClass() == clazz)
                return element;
        }

        return null;
    }


    public static Color getColor(Hud hud) {
        return hud.color.getValue();
    }

    public void drawSnappedText(GuiGraphicsExtractor gui, String text, int x, int y, int color, boolean shadow) {
        int textWidth = mc.font.width(text);
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        boolean snapRight = x > screenWidth / 2;
        int drawX = snapRight ? x + (textWidth + 2) - textWidth : x;

        drawX = Math.clamp(drawX, 0, screenWidth - textWidth);
        y = Math.clamp(y, 0, screenHeight - mc.font.lineHeight);

        gui.text(mc.font, text, drawX, y, color, shadow);
    }

    public void render(GuiGraphicsExtractor gui, DeltaTracker dt) {
        Hud hud = ModuleManager.INSTANCE.getModuleByClass(Hud.class);

        for (HudElement element : elements) {
            if (element.isVisible() && !shouldSkipRendering()) {
                element.renderInGame(gui, dt.getGameTimeDeltaTicks(), hud);
            }
        }

        List<NotificationWidget> toRemove = new ArrayList<>();
        for (NotificationWidget notification : notifications) {
            notification.setIndex(notifications.indexOf(notification));
            notification.render(gui, 0, 0, 0);
            if (System.currentTimeMillis() >= notification.popTime) {
                toRemove.add(notification);
            }
        }
        notifications.removeAll(toRemove);
    }

    private static boolean shouldSkipRendering() {
        Hud hud = ModuleManager.INSTANCE.getModuleByClass(Hud.class);
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

    public void pushNotification(String title, String desc) {
        notifications.push(new NotificationWidget(title, desc));
    }

    public List<HudElement> getElements() {
        return elements;
    }
}