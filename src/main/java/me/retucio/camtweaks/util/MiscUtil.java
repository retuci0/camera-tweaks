package me.retucio.camtweaks.util;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.client.HUD;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static me.retucio.camtweaks.CameraTweaks.mc;

public class MiscUtil {

    public static Screen screen;

    @SubscribeEvent
    public static void onTick(TickEvent.Post event) {
        if (screen != null && mc.currentScreen == null) {
            mc.setScreen(screen);
            screen = null;
        }
    }

    public static void copyVector(Vector3d destination, Vec3d source) {
        destination.x = source.x;
        destination.y = source.y;
        destination.z = source.z;
    }

    public static String getCurrentFormattedTime() {
        return getFormattedTime(System.currentTimeMillis());
    }

    public static String getFormattedTime(long timeMillis) {
        try {
            HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);
            Instant instant = Instant.ofEpochMilli(timeMillis);
            ZoneOffset offset = ZoneOffset.ofHours(hud.timezone.getIntValue());
            LocalTime time = LocalTime.from(instant.atOffset(offset));

            boolean is24 = hud.timeFormat.is(HUD.TimeFormat.TWENTY_FOUR_HOUR);
            DateTimeFormatter format = DateTimeFormatter.ofPattern(is24 ? "HH:mm" : "hh:mm a");
            return time.format(format);
        } catch (Exception e) {
            return "??:??";
        }
    }

    public static String removeAccentMarks(String text) {
        return text
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }

}
