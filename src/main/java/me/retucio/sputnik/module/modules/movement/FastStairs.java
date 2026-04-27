package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;


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
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.isSprinting() && onlyWhenSprinting.getValue()) return;

        Block nextBlock = mc.level.getBlockState(mc.player.blockPosition().offset(mc.player.getDirection().getUnitVec3i())).getBlock();
        if (!(nextBlock instanceof StairBlock && onlyWhenStairsNext.getValue())) return;

        if (mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() instanceof StairBlock
                && mc.player.zza > 0 && mc.player.onGround()) {
            mc.player.jumpFromGround();
        }
    }
}
