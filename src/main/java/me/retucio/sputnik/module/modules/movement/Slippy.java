package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.mixin.accessor.AbstractBlockAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.Map;

public class Slippy extends Module {

    public NumberSetting slipperiness = sgGeneral.add(new NumberSetting(
            "cantidad de semen",
            "cuánto semen agregar a los bloques por los que pasas",
            1,
            0,
            2,
            0.01
    ));

    private final Map<Block, Float> modifiedBlocks = new HashMap<>();

    public Slippy() {
        super("patinaje", "añade una fina capa de semen a los bloques que pisas", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        for (Block block : modifiedBlocks.keySet()) {
            ((AbstractBlockAccessor) block).setSlipperiness(modifiedBlocks.get(block));
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        Block block =  mc.world.getBlockState(mc.player.getBlockPos().add(new Vec3i(0, -1, 0))).getBlock();
        if (block.getSlipperiness() == slipperiness.getFloatValue()) return;
        if (!modifiedBlocks.containsKey(block)) modifiedBlocks.put(block, block.getSlipperiness());
        ((AbstractBlockAccessor) block).setSlipperiness(slipperiness.getFloatValue());
    }
}
