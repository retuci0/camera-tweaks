package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.RPackBypass;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerInfo.class)
public abstract class ServerInfoMixin {

    @Shadow private ServerInfo.ResourcePackPolicy resourcePackPolicy;
    @Unique private boolean isBypassStatus;

    @Unique
    private static RPackBypass bypassPack;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(String name, String address, ServerInfo.ServerType serverType, CallbackInfo ci) {
        bypassPack = ModuleManager.INSTANCE.getModuleByClass(RPackBypass.class);
    }

    @Inject(method = "fromNbt", at = @At("TAIL"))
    private static void addToRead(NbtCompound root, CallbackInfoReturnable<ServerInfo> cir) {
        if (!bypassPack.isEnabled()) return;
        if (root.getBoolean(bypassPack.TAG_NAME, false))
            cir.getReturnValue().setResourcePackPolicy(bypassPack.getPolicy());
    }

    @Inject(method = "toNbt", at = @At("HEAD"))
    private void preWrite(CallbackInfoReturnable<NbtCompound> cir) {
        if (!bypassPack.isEnabled()) return;
        if (this.resourcePackPolicy == bypassPack.getPolicy()) {
            this.isBypassStatus = true;
            this.resourcePackPolicy = ServerInfo.ResourcePackPolicy.PROMPT;
        }
    }

    @Inject(method = "toNbt", at = @At("TAIL"))
    private void addToWrite(CallbackInfoReturnable<NbtCompound> cir) {
        if (!bypassPack.isEnabled()) return;
        if (this.isBypassStatus) {
            cir.getReturnValue().putBoolean(bypassPack.TAG_NAME, true);
            this.resourcePackPolicy = bypassPack.getPolicy();
            this.isBypassStatus = false;
        }
    }
}
