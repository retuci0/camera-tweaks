package me.retucio.sputnik.mixin.mixins.misc;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ChatPlus;
import net.minecraft.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(StringUtil.class)
public abstract class StringUtilMixin {

    @ModifyArg(method = "trimChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringUtil;truncateStringIfNecessary(Ljava/lang/String;IZ)Ljava/lang/String;"), index = 1)
    private static int onTruncate(int maxLength) {
        ChatPlus chatPlus = ModuleManager.INSTANCE.getModuleByClass(ChatPlus.class);
        return ((chatPlus.isEnabled() && chatPlus.noCharLimit.getValue()) ? Integer.MAX_VALUE : maxLength);
    }
}