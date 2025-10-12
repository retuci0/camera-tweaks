package me.retucio.camtweaks.ui;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Freecam;
import me.retucio.camtweaks.module.modules.HUD.TimeFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

// en un principio esto iba a mostrar en pantalla los módulos activados, pero me parece innecesario. se queda aquí por si acaso
// igual lo uso para algo...
public class HUD {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private long lastPacketTime = 0;
    private static float estimatedTPS = 20f;

    public static void render(DrawContext ctx, RenderTickCounter tc) {
        if (ModuleManager.INSTANCE == null
                || mc.player == null
                || mc.getCameraEntity() == null
                || mc.currentScreen instanceof TitleScreen
                || mc.options.hudHidden)
            return;

        me.retucio.camtweaks.module.modules.HUD hud = ModuleManager.INSTANCE.getModuleByClass(me.retucio.camtweaks.module.modules.HUD.class);

        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        Color color;
        if (hud.rainbow.isEnabled()) {
            float speed = 10001 - hud.rainbowSpeed.getFloatValue();
            float hue = (System.currentTimeMillis() % (int) speed) / speed;
            Color gamning = Color.getHSBColor(hue, 1, 1);
            color = new Color(gamning.getRed(), gamning.getGreen(), gamning.getBlue(), hud.alpha.getIntValue());
        } else {
            color = new Color(hud.red.getIntValue(), hud.green.getIntValue(), hud.blue.getIntValue(), hud.alpha.getIntValue());
        }
        boolean shadow = hud.shadow.isEnabled();

        if (hud.isEnabled()) {

            // coordenadas
            if (hud.coords.isEnabled()) {
                Vec3d coords;
                Freecam freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
                if (freecam.isEnabled())
                    coords = new Vec3d(
                            freecam.getX(tc.getDynamicDeltaTicks()),
                            freecam.getY(tc.getDynamicDeltaTicks()),
                            freecam.getZ(tc.getDynamicDeltaTicks())
                    );
                else
                    coords = mc.player.getEntityPos();

                String text = (int) coords.x + " " + (int) coords.y + " " + (int) coords.z;
                ctx.drawText(mc.textRenderer, text,
                        (hud.coordsX.getIntValue() == -1) ? width - mc.textRenderer.getWidth(text) - 2 : hud.coordsX.getIntValue(),
                        (hud.coordsY.getIntValue() == -1) ? height - mc.textRenderer.fontHeight - 2 : hud.coordsY.getIntValue(),
                        color.getRGB(), shadow);
            }

            // fps y tps
            if (hud.fps.isEnabled())
                ctx.drawText(mc.textRenderer, "FPS: " + mc.getCurrentFps(), 2, 2, color.getRGB(), shadow);

            if (hud.tps.isEnabled())
                ctx.drawText(mc.textRenderer, "TPS: " + estimatedTPS, 2, 4 + mc.textRenderer.fontHeight, color.getRGB(), shadow);

            // marca de agua
            if (!hud.customText.getValue().isEmpty()) {
                int y;
                if (!mc.player.getActiveStatusEffects().isEmpty() && hud.dontOverride.isEnabled()) y = 26;
                else y = 2;
                ctx.drawText(mc.textRenderer, hud.customText.getValue(), width - mc.textRenderer.getWidth(hud.customText.getValue()) - 2, y, color.getRGB(), shadow);
            }

            // hora real
            if (hud.time.isEnabled()) {
                int y;
                if (mc.currentScreen instanceof ChatScreen && hud.dontOverride.isEnabled()) y = height - mc.textRenderer.fontHeight - 14;
                else y = height - mc.textRenderer.fontHeight - 2;

                ZoneOffset offset = ZoneOffset.ofHours(hud.timezone.getIntValue());
                LocalTime now = LocalTime.now(offset);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(hud.timeFormat.is(TimeFormat.TWENTY_FOUR_HOUR) ? "HH:mm" : "hh:mm a");
                String time = now.format(formatter);

                ctx.drawText(mc.textRenderer, time, 2, y, color.getRGB(), shadow);
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        // estimar el tps teniendo en cuenta el tiempo entre paquetes
        long now = System.currentTimeMillis();
        if (lastPacketTime > 0) {
            long diff = now - lastPacketTime;  // ms entre paquetes
            float tps = 1000f / diff;
            estimatedTPS = Math.min(20.0f, tps);
        }
        lastPacketTime = now;
    }
}