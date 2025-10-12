package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Nametags;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow
    public abstract ItemStack getStack();

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Text showItemCount(Text original) {
        Nametags nametags = ModuleManager.INSTANCE.getModuleByClass(Nametags.class);
        if (!nametags.isEnabled()) return original;

        int count = this.getStack().getCount();
        if (nametags.countItems.isEnabled() && count > 1) return original.copy().append(" x" + count);
        return original;
    }
}