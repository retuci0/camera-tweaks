package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.KeyEvent;
import me.retucio.camtweaks.event.events.RenderWorldEvent;
import me.retucio.camtweaks.event.events.camtweaks.AddEntityEvent;
import me.retucio.camtweaks.event.events.camtweaks.RemoveEntityEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.render.RenderUtil;

import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class LogoutSpots extends Module {

    public KeySetting clearBoxesKey = addSetting(new KeySetting("vaciar cajas", "tecla asignada a olvidar todas las cajas activas", GLFW.GLFW_KEY_UNKNOWN));

    public BooleanSetting filledBox = addSetting(new BooleanSetting("caja rellena", "rellenar caja", true));
    public BooleanSetting outlineBox = addSetting(new BooleanSetting("caja de esquinas", "contorno de caja", true));

    public NumberSetting red = addSetting(new NumberSetting("rojo", "cantidad de rojo", 200, 0, 255, 1));
    public NumberSetting green = addSetting(new NumberSetting("verde", "cantidad de verde", 90, 0, 255, 1));
    public NumberSetting blue = addSetting(new NumberSetting("azul", "cantidad de azul", 255, 0, 255, 1));
    public NumberSetting alpha = addSetting(new NumberSetting("opacidad", "antitransparencia", 50, 0, 255, 1));

    public NumberSetting lineWidth = addSetting(new NumberSetting("grosor", "grosor de las líneas a dibujar", 2, 1, 10, 0.5f));

    private final List<LogoutBox> boxes = new ArrayList<>();

    public LogoutSpots() {
        super("puntos de desconexión", "te muestra los puntos donde se desconectan los jugadores");
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldEvent event) {
        if (mc.player == null || mc.world == null) return;

        int r = red.getIntValue();
        int g = green.getIntValue();
        int b = blue.getIntValue();
        int a = alpha.getIntValue();

        MatrixStack matrices = event.getMatrices();

        for (LogoutBox box : boxes) {
            if (filledBox.isEnabled())
                RenderUtil.drawFilledBox(matrices, box.dummy.getBoundingBox(), new Color(r, g, b, a));
            if (outlineBox.isEnabled())
                RenderUtil.drawOutlineBox(matrices, box.dummy.getBoundingBox(), new Color(r, g, b, a), lineWidth.getFloatValue());
        }
    }

    @SubscribeEvent
    public void onEntityRemoved(RemoveEntityEvent event) {
        if (event.getEntity() instanceof PlayerEntity player && player != mc.player) {
            String playerName = player.getName().getString() + " (" + getLogoutTime(System.currentTimeMillis()) + ")";

            boxes.add(new LogoutBox(
                    ModuleManager.INSTANCE.getModuleByClass(FakePlayer.class).addPlayer(player, playerName),
                    System.currentTimeMillis()
            ));
        }
    }

    @SubscribeEvent
    public void onEntityAdded(AddEntityEvent event) {
        boxes.removeIf(box -> event.getEntity().getUuid().equals(box.dummy.getUuid()));
    }

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        if (event.getKey() == clearBoxesKey.getKey()) {
            boxes.forEach(box -> {
                box.dummy.setRemoved(Entity.RemovalReason.KILLED);
                box.dummy.onRemoved();
            });

            boxes.clear();
        }
    }

    private String getLogoutTime(long logoutTimeMillis) {
        HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);

        Instant instant = Instant.ofEpochMilli(logoutTimeMillis);

        ZoneOffset offset = ZoneOffset.ofHours(hud.timezone.getIntValue());
        LocalTime logoutTime = LocalTime.from(instant.atOffset(offset));

        boolean is24 = hud.timeFormat.is(HUD.TimeFormat.TWENTY_FOUR_HOUR);
        DateTimeFormatter format = DateTimeFormatter.ofPattern(is24 ? "HH:mm" : "hh:mm a");

        return logoutTime.format(format);
    }

    public record LogoutBox(
            OtherClientPlayerEntity dummy,
            long logoutTime
    ) {}
}
