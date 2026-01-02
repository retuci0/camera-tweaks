package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.network.RPackBypass;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(ServerInfo.ResourcePackPolicy.class)
public abstract class ResourcePackPolicyMixin {

    @Unique
    private static ServerInfo.ResourcePackPolicy BYPASS;

    @Invoker("<init>")
    public static ServerInfo.ResourcePackPolicy init(final String enumName, final int enumOrdinal, final String name) {
        throw new AssertionError();
    }

    @Inject(method = "values", at = @At("TAIL"), cancellable = true)
    private static void addVariant(CallbackInfoReturnable<ServerInfo.ResourcePackPolicy[]> cir) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (!bypassPack.isEnabled()) return;

        ServerInfo.ResourcePackPolicy[] values = cir.getReturnValue();
        final int ordinal = values.length;
        cir.setReturnValue(values = Arrays.copyOfRange(values, 0, ordinal + 1));
        values[ordinal] = BYPASS = init(bypassPack.ENUM_NAME, ordinal, "bypass");
    }

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void addToGetName(final CallbackInfoReturnable<Text> cir) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (BYPASS == (Object) this && bypassPack.isEnabled())
            cir.setReturnValue(bypassPack.BYPASS_TEXT);
    }
}