package me.retucio.sputnik.mixin.mixins.network;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.network.RPackBypass;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;


@Mixin(ServerData.ServerPackStatus.class)
public abstract class ServerPackStatusMixin {

    @Unique
    private static ServerData.ServerPackStatus BYPASS;

    @Invoker("<init>")
    public static ServerData.ServerPackStatus init(final String enumName, final int enumOrdinal, final String name) {
        throw new AssertionError();
    }

    @Inject(method = "values", at = @At("TAIL"), cancellable = true)
    private static void addVariant(CallbackInfoReturnable<ServerData.ServerPackStatus[]> cir) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (!bypassPack.isEnabled()) return;

        ServerData.ServerPackStatus[] values = cir.getReturnValue();
        final int ordinal = values.length;
        cir.setReturnValue(values = Arrays.copyOfRange(values, 0, ordinal + 1));
        values[ordinal] = BYPASS = init(bypassPack.ENUM_NAME, ordinal, "bypass");
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void addToGetName(final CallbackInfoReturnable<Component> cir) {
        RPackBypass bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
        if (BYPASS == (Object) this && bypassPack.isEnabled())
            cir.setReturnValue(bypassPack.BYPASS_TEXT);
    }
}