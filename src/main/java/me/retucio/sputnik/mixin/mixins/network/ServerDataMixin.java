package me.retucio.sputnik.mixin.mixins.network;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.network.RPackBypass;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ServerData.class)
public abstract class ServerDataMixin {

    @Shadow private ServerData.ServerPackStatus packStatus;
    @Unique private boolean isBypassStatus;

    @Unique
    private static RPackBypass bypassPack;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(String name, String ip, ServerData.Type type, CallbackInfo ci) {
        bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
    }

    @Inject(method = "read", at = @At("TAIL"))
    private static void addToRead(CompoundTag tag, CallbackInfoReturnable<ServerData> cir) {
        if (!bypassPack.isEnabled()) return;
        if (tag.getBooleanOr(bypassPack.TAG_NAME, false)) {
            cir.getReturnValue().setResourcePackStatus(bypassPack.getStatus());
        }
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void preWrite(CallbackInfoReturnable<CompoundTag> cir) {
        if (!bypassPack.isEnabled()) return;
        if (this.packStatus == bypassPack.getStatus()) {
            this.isBypassStatus = true;
            this.packStatus = ServerData.ServerPackStatus.PROMPT;
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void addToWrite(CallbackInfoReturnable<CompoundTag> cir) {
        if (!bypassPack.isEnabled()) return;
        if (this.isBypassStatus) {
            cir.getReturnValue().putBoolean(bypassPack.TAG_NAME, true);
            this.packStatus = bypassPack.getStatus();
            this.isBypassStatus = false;
        }
    }
}
