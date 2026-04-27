package me.retucio.sputnik.module.modules.movement;

import com.google.common.collect.Streams;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

/**
 * @link <a href="https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Parkour.java">yes i'm a skid</a>
 * @"author" retucio
 */
public class Parkour extends Module {

    private final BooleanSetting onlyWhenSprinting = sgGeneral.add(new BooleanSetting(
            "solo al correr",
            "solo aplicar salto al estar esprintando",
            false
    ));

    private final BooleanSetting dontWhenSneaking = sgGeneral.add(new BooleanSetting(
            "no al agacharse",
            "no saltar al estar agachado",
            false
    ));

    private final NumberSetting offset = sgGeneral.add(new NumberSetting(
            "offset",
            "cuán antes saltar",
            0.001,
            0.001,
            0.6,
            0.001
    ));

    public Parkour() {
        super("parkour",
                "salta automáticamente en el último tick posible, en la esquina del bloque",
                Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (!(mc.player.isSprinting() || mc.options.keySprint.isDown()) && onlyWhenSprinting.getValue()) return;
        if ((mc.player.isCrouching() || mc.options.keyShift.isDown()) && dontWhenSneaking.getValue()) return;

        if(!mc.player.onGround() || mc.options.keyJump.isDown()) return;

        AABB box = mc.player.getBoundingBox();
        AABB adjustedBox = box.move(0, -0.5, 0).inflate(-offset.getValue(), 0, -offset.getValue());

        Stream<VoxelShape> blockCollisions = Streams.stream(mc.level.getBlockCollisions(mc.player, adjustedBox));
        if (blockCollisions.findAny().isPresent()) return;

        mc.player.jumpFromGround();
    }
}
