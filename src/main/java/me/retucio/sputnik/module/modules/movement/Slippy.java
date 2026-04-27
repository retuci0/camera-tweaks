package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.mixin.accessors.BlockBehaviourAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;


public class Slippy extends Module {

    private final NumberSetting slipperiness = sgGeneral.add(new NumberSetting(
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
            ((BlockBehaviourAccessor) block).setSlipperiness(modifiedBlocks.get(block));
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        Block block =  mc.level.getBlockState(mc.player.blockPosition().offset(new Vec3i(0, -1, 0))).getBlock();
        if (block.getFriction() == slipperiness.getFloatValue()) return;
        if (!modifiedBlocks.containsKey(block)) modifiedBlocks.put(block, block.getFriction());
        ((BlockBehaviourAccessor) block).setSlipperiness(slipperiness.getFloatValue());
    }
}
