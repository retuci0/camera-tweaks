package me.retucio.sputnik.module.modules.world;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.util.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.tag.ItemTags;

public class NoMiningInterruptions extends Module {

    public BooleanSetting withPickaxeOnly = sgGeneral.add(new BooleanSetting("solo con pico", "ignorar entidades solamente cuando se sujeta un pico", false));
    public ListSetting<EntityType<?>> entities = sgGeneral.add(new ListSetting<>("entidades", "entidades a ignorar",
            Lists.entityList, Lists.allTrue(Lists.entityList), Lists.entityNames));

    public NoMiningInterruptions() {
        super("minar sin interrupción",
                "te permite minar bloques a través de entidades",
                Category.WORLD);
    }

    public boolean shouldIgnoreEntity(Entity entity) {
        if (entity == null || !isEnabled() || mc.player == null) return false;

        boolean pickaxe = true;
        if (withPickaxeOnly.getValue())
            pickaxe = (mc.player.getMainHandStack().isIn(ItemTags.PICKAXES)
                    || mc.player.getOffHandStack().isIn(ItemTags.PICKAXES));

        return entities.isEnabled(entity.getType()) && pickaxe;
    }
}
