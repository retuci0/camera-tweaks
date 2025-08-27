package me.retucio.camtweaks.util;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import static me.retucio.camtweaks.CameraTweaks.mc;

public class MiscUtil {

    public static Screen screen;

    private MiscUtil() {}

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

    public static Vector3d getEntityVector(Vector3d vector, Entity entity, double tickDelta) {
        vector.x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        vector.y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        vector.z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
        return vector;
    }
}
