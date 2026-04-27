package me.retucio.sputnik.mixin.mixins.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.Nametags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow
    public abstract ItemStack getItem();

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component showItemCount(Component original) {
        Nametags nametags = ModuleManager.INSTANCE.getModuleByClass(Nametags.class);
        if (!nametags.isEnabled()) return original;

        int count = this.getItem().getCount();
        MutableComponent name = this.getItem().getCustomName() != null ? this.getItem().getCustomName().plainCopy() : original.plainCopy();

        if (!name.equals(original)) name = name.withStyle(ChatFormatting.ITALIC);

        if (nametags.countItems.getValue()) {
            if (count > 1) return name.copy().append(" x" + count);
            return name;
        }

        return original;
    }
}