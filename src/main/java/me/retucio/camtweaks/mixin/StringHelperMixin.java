package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.misc.ChatPlus;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(StringHelper.class)
public abstract class StringHelperMixin {

    @ModifyArg(method = "truncateChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;truncate(Ljava/lang/String;IZ)Ljava/lang/String;"), index = 1)
    private static int onTruncate(int maxLength) {
        ChatPlus chatPlus = ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class);
        return ((chatPlus.isEnabled() && chatPlus.noCharLimit.isEnabled()) ? Integer.MAX_VALUE : maxLength);
    }
}