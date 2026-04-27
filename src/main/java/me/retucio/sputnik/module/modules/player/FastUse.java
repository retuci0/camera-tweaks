package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;


public class FastUse extends Module {

    private final BooleanSetting items = sgGeneral.add(new BooleanSetting(
            "items",
            "usar items precozmente",
            true
    ));

    private final NumberSetting itemCooldown = sgGeneral.add(new NumberSetting(
            "cooldown de items",
            "cooldown para usar items en ticks",
            0,
            0,
            4,
            1
    ));


    private final BooleanSetting blocks = sgGeneral.add(new BooleanSetting(
            "bloques",
            "colocar bloques precozmente",
            true
    ));

    private final NumberSetting blockCooldown = sgGeneral.add(new NumberSetting(
            "cooldown de bloques",
            "cooldown para colocar bloques en ticks",
            0,
            0,
            4,
            1
    ));


    public final BooleanSetting mining = sgGeneral.add(new BooleanSetting(
            "minar",
            "minar precozmente",
            true
    ));

    public final NumberSetting miningCooldown = sgGeneral.add(new NumberSetting(
            "cooldown al minar",
            "cooldown entre rotura de bloques en ticks",
            5, 0, 20, 1
    ));


    public FastUse() {
        super("precoz",
                "hace cosas como usar items o colocar bloques más rápido",
                Category.PLAYER,
                GLFW.GLFW_KEY_F6);

        items.onUpdate(itemCooldown::visibility);
        blocks.onUpdate(blockCooldown::visibility);
    }

    public int getCooldown(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem && blocks.getValue()) return blockCooldown.getIntValue();
        if (items.getValue() && !(stack.getItem() instanceof BlockItem)) return itemCooldown.getIntValue();
        else return 4;  // cooldown por defecto, 4 ticks
    }
}
