package me.retucio.sputnik.module.modules.movement;

import com.google.common.collect.Streams;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

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
        if (mc.player == null || mc.world == null) return;

        if (!(mc.player.isSprinting() || mc.options.sprintKey.isPressed()) && onlyWhenSprinting.getValue()) return;
        if ((mc.player.isSneaking() || mc.options.sneakKey.isPressed()) && dontWhenSneaking.getValue()) return;

        if(!mc.player.isOnGround() || mc.options.jumpKey.isPressed()) return;

        Box box = mc.player.getBoundingBox();
        Box adjustedBox = box.offset(0, -0.5, 0).expand(-offset.getValue(), 0, -offset.getValue());

        Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));
        if (blockCollisions.findAny().isPresent()) return;

        mc.player.jump();
    }
}
