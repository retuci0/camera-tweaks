package me.retucio.sputnik.module.modules.movement;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.interact.ClipAtLedgeEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class SafeWalk extends Module {

    private final NumberSetting fallDistance = sgGeneral.add(new NumberSetting("distancia de caída", "distancia de caída máxima permitida", 1, 0, 10, 0.1));

    public SafeWalk() {
        super("muletas",
                "te ayuda a no caerte de bloques, sin agacharte",
                Category.MOVEMENT);
    }

    @EventListener
    private void onClipAtLedge(ClipAtLedgeEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (fallDistance.getValue() > 1) {
            int surface = mc.level.getChunkAt(
                    mc.player.blockPosition())
                    .getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING)
                    .getFirstAvailable(mc.player.getBlockX() & 15, mc.player.getBlockZ() & 15);

            if (mc.player.getBlockY() >= surface)
                if (mc.player.getBlockY() - surface < fallDistance.getValue()) return;

            else {
                BlockHitResult raycastResult = mc.level.clip(new ClipContext(
                        mc.player.position(),
                        new Vec3(mc.player.getX(),
                                mc.level.getMinY(),
                                mc.player.getZ()),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.WATER,
                        mc.player));

                if (raycastResult.getType() != HitResult.Type.MISS)
                    if ((int) (mc.player.getY() - raycastResult.getBlockPos().above().getY()) < fallDistance.getValue()) return;
            }
        }

        if (!mc.player.isCrouching())
            event.setClipping(true);
    }
}
