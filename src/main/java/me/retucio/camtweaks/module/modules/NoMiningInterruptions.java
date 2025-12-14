package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.ItemTags;

public class NoMiningInterruptions extends Module {

    public BooleanSetting withPickaxeOnly = addSetting(new BooleanSetting("solo con pico", "ignorar entidades solamente cuando se sujeta un pico", false));

    public NoMiningInterruptions() {
        super("minar sin interrupción", "te permite minar bloques a través de entidades");
    }

    public boolean shouldIgnoreEntity(Entity entity) {
        if (entity == null || !this.isEnabled()) return false;
        if (withPickaxeOnly.isEnabled())
            return (mc.player.getMainHandStack().isIn(ItemTags.PICKAXES) || mc.player.getOffHandStack().isIn(ItemTags.PICKAXES));
        return true;
    }


}
