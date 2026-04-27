package me.retucio.sputnik.mixin.mixins.item;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.inventory.ShulkerPeek;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void onAppendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag, CallbackInfo ci) {
        ShulkerPeek shulkerPeek = ModuleManager.INSTANCE.getModuleByClass(ShulkerPeek.class);
        if (!shulkerPeek.isEnabled() || !shulkerPeek.showTooltips.getValue()) return;

        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            if (ShulkerPeek.isShulkerEmpty(stack)) {
                builder.accept(Component.literal("vacío").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
                builder.accept(
                        Component.literal("mantén ")
                                .append(Component.literal(shulkerPeek.previewKey.getKeyName())
                                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                                .append(" para previsualizar")
                                .withStyle(ChatFormatting.GRAY)
                );
            }
        }
    }
}
