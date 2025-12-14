package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.ShulkerPeek;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    private void onAppendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type, CallbackInfo ci) {
        ShulkerPeek shulkerPeek = ModuleManager.INSTANCE.getModuleByClass(ShulkerPeek.class);
        if (!shulkerPeek.isEnabled() || !shulkerPeek.showTooltips.isEnabled()) return;

        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            if (ShulkerPeek.isShulkerEmpty(stack)) {
                textConsumer.accept(Text.literal("vacío").formatted(Formatting.GRAY, Formatting.ITALIC));
            } else {
                textConsumer.accept(
                        Text.literal("mantén ")
                                .append(Text.literal(shulkerPeek.previewKey.getKeyName())
                                        .formatted(Formatting.GOLD, Formatting.BOLD))
                                .append(" para previsualizar")
                                .formatted(Formatting.GRAY)
                );
            }
        }
    }
}
