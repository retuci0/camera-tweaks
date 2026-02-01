package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.interact.BlockShapeEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

public class Jesus extends Module {

    private final SettingGroup sgBlocks = addSg(new SettingGroup("bloques", true));

    private final EnumSetting<JesusMode> mode = sgGeneral.add(new EnumSetting<>(
            "modo",
            "cómo caminar",
            JesusMode.class,
            JesusMode.SOLID
    ));

    private final BooleanSetting allowSneaking = sgGeneral.add(new BooleanSetting(
            "permitir agacharse",
            "te permite usar el botón de agacharse para sumergirte en el agua",
            true
    ));

    private final NumberSetting minFallHeight = sgGeneral.add(new NumberSetting(
            "distancia de caída",
            "distancia de caída mínima para sumergirse en vez de impactar",
            3,
            0.1,
            10,
            0.1
    ));

    private final BooleanSetting water = sgBlocks.add(new BooleanSetting(
            "agua",
            "permitir caminar en agua",
            true
    ));

    private final BooleanSetting lava = sgBlocks.add(new BooleanSetting(
            "lava",
            "permitir caminar en lava",
            true
    ));

    private final BooleanSetting powderedSnow = sgBlocks.add(new BooleanSetting(
            "nieve en polvo",
            "permitir caminar en nieve en polvo",
            true
    ));

    public Jesus() {
        super("jesús", "te permite caminar sobre el agua", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        Entity entity = mc.player.getRootVehicle();

        if (entity.isSneaking() && allowSneaking.getValue()) {
            return;
        }

        if (entity.fallDistance >= minFallHeight.getValue()) {
            return;
        }

        if (!mode.is(JesusMode.VIBRATE)) return;

        if (isSubmerged(entity.getEntityPos().add(new Vec3d(0, 0.3, 0)))) {
            entity.setVelocity(entity.getVelocity().x, 0.08, entity.getVelocity().z);
        }

        else if (isSubmerged(entity.getEntityPos().add(new Vec3d(0, 0.1, 0)))) {
            entity.setVelocity(entity.getVelocity().x, 0.05, entity.getVelocity().z);
        }

        else if (isSubmerged(entity.getEntityPos().add(new Vec3d(0, 0.05, 0)))) {
            entity.setVelocity(entity.getVelocity().x, 0.01, entity.getVelocity().z);
        }

        else if (isSubmerged(entity.getEntityPos())) {
            entity.setVelocity(entity.getVelocity().x, -0.005, entity.getVelocity().z);
            entity.setOnGround(true);
        }
    }

    @SubscribeEvent
    private void onBlockShape(BlockShapeEvent event) {
        if (event.getState().getBlock() == Blocks.LAVA && !lava.getValue()) return;
        if (event.getState().getBlock() == Blocks.WATER && !water.getValue()) return;
        if (event.getState().getBlock() == Blocks.POWDER_SNOW && !powderedSnow.getValue()) return;
        if (mode.getValue() == JesusMode.SOLID)
            if (event.getState().getBlock() == Blocks.POWDER_SNOW
                    && !mc.player.isSneaking()
                    && mc.player.getY() > event.getPos().getY() + 1) {
                event.setShape(VoxelShapes.cuboid(0, 0, 0, 1, 1.01, 1));
            } else if (!mc.world.getFluidState(event.getPos()).isEmpty()
                    && !mc.player.isSneaking()
                    && !mc.player.isTouchingWater()
                    && mc.player.getY() >= event.getPos().getY() + 0.9) {
                event.setShape(VoxelShapes.cuboid(0, 0, 0, 1, 0.9, 1));
        }
    }

    private boolean isSubmerged(Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        FluidState state = mc.world.getFluidState(blockPos);

        return !state.isEmpty() && pos.y - blockPos.getY() <= state.getHeight();
    }

    private enum JesusMode {
        VIBRATE("vibrador"),
        SOLID("sólido");

        private final String name;
        JesusMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
