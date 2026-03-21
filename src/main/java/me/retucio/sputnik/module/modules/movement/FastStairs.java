package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;

public class FastStairs extends Module {

    private final BooleanSetting onlyWhenSprinting = sgGeneral.add(new BooleanSetting(
            "solo al correr",
            "solo saltar al estar esprintando",
            true
    ));

    private final BooleanSetting onlyWhenStairsNext = sgGeneral.add(new BooleanSetting(
            "solo al haber más escaleras",
            "solo saltar cuando el siguiente bloque también son escaleras",
            true
    ));

    public FastStairs() {
        super("escaleras veloces", "sube escaleras más rápidamente", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isSprinting() && onlyWhenSprinting.getValue()) return;

        Block nextBlock = mc.world.getBlockState(mc.player.getBlockPos().add(mc.player.getHorizontalFacing().getVector())).getBlock();
        if (!(nextBlock instanceof StairsBlock && onlyWhenStairsNext.getValue())) return;

        if (mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() instanceof StairsBlock
                && mc.player.forwardSpeed > 0 && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }
}
