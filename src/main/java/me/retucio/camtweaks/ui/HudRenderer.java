package me.retucio.camtweaks.ui;

import me.retucio.camtweaks.config.ConfigManager;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import me.retucio.camtweaks.event.events.camtweaks.UpdateSettingEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Freecam;
import me.retucio.camtweaks.module.modules.HUD;
import me.retucio.camtweaks.ui.screen.HudEditorScreen;
import me.retucio.camtweaks.ui.widgets.HudElement;

import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HudRenderer {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // tps
    private static float estimatedTPS = 20f;
    private static final List<Float> tpsHistory = new ArrayList<>();
    private long lastWorldTime = -1L;
    private long lastRealTime = -1L;

    private static final Map<String, int[]> positions = ConfigManager.getConfig().hudPositions;
    private static final Map<String, Boolean> visibilities = ConfigManager.getConfig().hudVisibilities;
    private static final List<HudElement> elements = new ArrayList<>();
    private static boolean initialized = false;

    private static HudElement customTextElement = null;

    public static void initElements() {
        if (initialized) return;
        initialized = true;

        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);

        // coordenadas
        addElement("coords", 2, mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight, (ctx, mx, my, delta) -> {
            drawSnappedText(ctx, getElementText("coords", delta, hud), mx, my, getColor(hud).getRGB(), hud.shadow.isEnabled());
        });

        // FPS
        addElement("fps", 2, 2, (ctx, mx, my, delta) -> {
            drawSnappedText(ctx, getElementText("fps", 0, hud), mx, my, getColor(hud).getRGB(), hud.shadow.isEnabled());
        });

        // TPS
        addElement("tps", 2, mc.textRenderer.fontHeight + 4, (ctx, mx, my, delta) -> {
            drawSnappedText(ctx, getElementText("tps", delta, hud), mx, my, getColor(hud).getRGB(), hud.shadow.isEnabled());
        });

        // texto custom
        addElement("customText", mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(getElementText("customText", 0, ModuleManager.INSTANCE.getModuleByClass(HUD.class))) / 2, 2, (ctx, mx, my, delta) -> {
            drawSnappedText(ctx, getElementText("customText", delta, hud), mx, my, getColor(hud).getRGB(), hud.shadow.isEnabled());
        });

        customTextElement = elements.stream()
                .filter(e -> e.getId().equals("customText"))
                .findFirst()
                .orElse(null);

        // hora
        addElement("time", mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth("04:20"), mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 2, (ctx, mx, my, delta) -> {
            drawSnappedText(ctx, getElementText("time", delta, hud), mx, my, getColor(hud).getRGB(), hud.shadow.isEnabled());
        });

        // ping
        addElement("ping", 2, 2 * (mc.textRenderer.fontHeight + 4), (ctx, mx, my, delta) -> {
            drawSnappedText(ctx, getElementText("ping", delta, hud), mx, my, getColor(hud).getRGB(), hud.shadow.isEnabled());
        });

        HudEditorScreen.INSTANCE.setElements(getElements());
    }

    private static void addElement(String id, int defX, int defY, HudElement.Renderer renderer) {
        int[] saved = positions.get(id);
        Boolean visible = visibilities.get(id);
        int x = saved == null ? defX : saved[0];
        int y = saved == null ? defY : saved[1];
        boolean v = visible == null || visible;
        if (visibilities.isEmpty()) v = true;

        elements.add(new HudElement(id, defX, defY, x, y, v) {
            @Override public void render(DrawContext ctx, int mx, int my, float delta) {
                renderer.render(ctx, x, y, delta);
            }
        });
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

        // clampear y
        y = Math.max(0, Math.min(y, screenHeight - mc.textRenderer.fontHeight));

        // clampear x
        drawX = Math.max(0, Math.min(drawX, screenWidth - textWidth));

        ctx.drawText(mc.textRenderer, text, drawX, y, color, shadow);
    }

    public static void render(DrawContext ctx, RenderTickCounter tc) {
        if (ModuleManager.INSTANCE == null
                || mc.player == null
                || mc.getCameraEntity() == null
                || mc.currentScreen instanceof TitleScreen
                || mc.options.hudHidden)
            return;

        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (!hud.isEnabled()) return;
        if (mc.debugHudEntryList.isF3Enabled() && !hud.showOnF3.isEnabled()) return;
        if (mc.currentScreen instanceof ChatScreen && !hud.showOnChat.isEnabled()) return;

        initElements();

        if (mc.currentScreen instanceof HudEditorScreen) return;

        float deltaTime = tc.getDynamicDeltaTicks();
        for (HudElement element : elements)
            if (element.isVisible()) element.render(ctx, 0, 0, deltaTime);
    }

    public static List<HudElement> getElements() {
        return elements;
    }

    // texto a dibujar por elemento
    public static String getElementText(String id, float delta, HUD hud) {
        switch (id) {
            case "coords" -> {
                Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
                Vec3d pos = freecam.isEnabled()
                        ? new Vec3d(freecam.getX(delta), freecam.getY(delta), freecam.getZ(delta))
                        : mc.player.getEntityPos();

                return (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
            }
            case "fps" -> {
                return "FPS: " + mc.getCurrentFps();
            }
            case "tps" -> {
                return String.format("TPS: %.1f", estimatedTPS);
            }
            case "customText" -> {
                return ModuleManager.INSTANCE.getModuleByClass(HUD.class).customText.getValue();
            }
            case "time" -> {
                ZoneOffset offset = ZoneOffset.ofHours(hud.timezone.getIntValue());
                LocalTime now = LocalTime.now(offset);

                boolean is24 = hud.timeFormat.is(HUD.TimeFormat.TWENTY_FOUR_HOUR);
                DateTimeFormatter format = DateTimeFormatter.ofPattern(is24 ? "HH:mm" : "hh:mm a");
                return now.format(format);
            }
            case "ping" -> {
                if (mc.getNetworkHandler() == null || mc.player == null) return "? ms";
                if (mc.isInSingleplayer()) return "-1 ms";

                PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                return playerListEntry != null ? playerListEntry.getLatency() + " ms" : "? ms";
            }
            default -> { return id; }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket packet) {
            long currentWorldTime = packet.time();
            long currentRealTime = System.currentTimeMillis();

            if (lastWorldTime != -1L && lastRealTime != -1L) {
                long elapsedRealTime = currentRealTime - lastRealTime;
                long elapsedWorldTicks = currentWorldTime - lastWorldTime;

                if (elapsedRealTime > 0) {
                    // TPS = (ticks passed) / (seconds passed)
                    float tps = (float) elapsedWorldTicks / (elapsedRealTime / 1000.0f);

                    // clampear
                    tps = Math.max(0.1f, Math.min(20.0f, tps));

                    // usar historial para suavizar resultados
                    tpsHistory.add(tps);
                    if (tpsHistory.size() > 10)
                        tpsHistory.removeFirst();

                    // calcular media de tps
                    float sum = 0;
                    for (float t : tpsHistory) sum += t;
                    estimatedTPS = sum / tpsHistory.size();
                }
            }

            lastWorldTime = currentWorldTime;
            lastRealTime = currentRealTime;
        }
    }

    /** para cuando se cambie el valor del texto custom */
    @SubscribeEvent
    public void onUpdateSetting(UpdateSettingEvent event) {
        if (ModuleManager.INSTANCE == null) return;
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
        if (event.getSetting() == hud.customText) {
            if (customTextElement != null) {
                int newX = mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(hud.customText.getValue()) / 2;
                customTextElement.setX(newX);
            }
        }
    }
}
