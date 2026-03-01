package me.retucio.sputnik.util;

import me.retucio.sputnik.event.TickEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static me.retucio.sputnik.Sputnik.mc;

public class MiscUtil {

    public static Screen screen;

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

    @SuppressWarnings("unchecked")
    public static <T> T getHighest(List<T> list) {
        if (list.getFirst() == null || !(list.getFirst() instanceof Number value)) return null;
        Number maxValue = value;
        for (T t : list) {
            if (((Number) t).doubleValue() > maxValue.doubleValue()) {
                maxValue = (Number) t;
            }
        }

        return (T) maxValue;
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

    public static String getPasteContent(int maxLength) {
        if (maxLength == -1) return mc.keyboard.getClipboard();
        return mc.keyboard.getClipboard().substring(0, Math.min(maxLength, mc.keyboard.getClipboard().length()));
    }

    public static void backspace(StringBuilder buffer) {
        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            for (int i = buffer.length() - 1; i >= 0; i--) {
                char c = buffer.charAt(i);
                buffer.deleteCharAt(i);
                if (c == ' ') break;
            }
        } else {
            if (!buffer.isEmpty()) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
        }
    }


    public static Vec3d vec3dOf(Vector3f v) {
        return new Vec3d(v.x, v.y, v.z);
    }

    public static Vector3f vec3fOf(Vec3d v) {
        return new Vector3f((float) v.x, (float) v.y, (float) v.z);
    }
}
