package me.retucio.camtweaks.module.modules.player;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.ClipAtLedgeEvent;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.NumberSetting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;

public class SafeWalk extends Module {

    public NumberSetting fallDistance = addSetting(new NumberSetting("distancia de caída", "distancia de caída máxima permitida", 1, 0, 10, 0.1));

    public SafeWalk() {
        super("muletas",
                "te ayuda a no caerte de bloques, sin agacharte",
                Category.PLAYER);
    }

    @SubscribeEvent
    public void onClipAtLedge(ClipAtLedgeEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (fallDistance.getValue() > 1) {
            int surface = mc.world.getWorldChunk(
                    mc.player.getBlockPos())
                    .getHeightmap(Heightmap.Type.MOTION_BLOCKING)
                    .get(mc.player.getBlockX() & 15, mc.player.getBlockZ() & 15);

            if (mc.player.getBlockY() >= surface)
                if (mc.player.getBlockY() - surface < fallDistance.getValue()) return;

            else {
                BlockHitResult raycastResult = mc.world.raycast(new RaycastContext(
                        mc.player.getEntityPos(),
                        new Vec3d(mc.player.getX(),
                                mc.world.getBottomY(),
                                mc.player.getZ()),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.WATER,
                        mc.player));

                if (raycastResult.getType() != HitResult.Type.MISS)
                    if ((int) (mc.player.getY() - raycastResult.getBlockPos().up().getY()) < fallDistance.getValue()) return;
            }
        }

        if (!mc.player.isSneaking())
            event.setClipping(true);
    }
}
