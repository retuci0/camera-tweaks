package me.retucio.camtweaks.module.modules.player;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class FastUse extends Module {

    public BooleanSetting items = addSetting(new BooleanSetting("items", "usar items precozmente", true));
    public NumberSetting itemCooldown = addSetting(new NumberSetting("cooldown de items", "cooldown para usar items en ticks", 0, 0, 4, 1));

    public BooleanSetting blocks = addSetting(new BooleanSetting("bloques", "colocar bloques precozmente", true));
    public NumberSetting blockCooldown = addSetting(new NumberSetting("cooldown de bloques", "cooldown para colocar bloques en ticks", 0, 0, 4, 1));

    public FastUse() {
        super("precoz",
                "hace cosas como usar items o colocar bloques más rápido",
                Category.PLAYER);

        items.onUpdate(v -> itemCooldown.setVisible(v));
        blocks.onUpdate(v -> blockCooldown.setVisible(v));
    }

    public int getCooldown(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem && blocks.isEnabled()) return blockCooldown.getIntValue();
        if (items.isEnabled() && !(stack.getItem() instanceof BlockItem)) return itemCooldown.getIntValue();
        else return 4;  // cooldown por defecto, 4 ticks
    }
}
