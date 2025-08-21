package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.modules.BetterChat;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static me.retucio.camtweaks.CameraTweaks.moduleManager;

@Mixin(StringHelper.class)
public abstract class StringHelperMixin {
    @ModifyArg(method = "truncateChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;truncate(Ljava/lang/String;IZ)Ljava/lang/String;"), index = 1)
    private static int truncate(int maxLength) {
        BetterChat betterChat = moduleManager.getModuleByClass(BetterChat.class);
        return ((betterChat.isEnabled() && betterChat.noCharLimit.isEnabled()) ? Integer.MAX_VALUE : maxLength);
    }
}