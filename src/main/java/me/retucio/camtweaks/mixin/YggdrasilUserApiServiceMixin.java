package me.retucio.camtweaks.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import com.mojang.authlib.yggdrasil.response.UserAttributesResponse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// código robado de: https://github.com/kb-1000/no-telemetry

@Mixin(YggdrasilUserApiService.class)
public abstract class YggdrasilUserApiServiceMixin {

    @Redirect(method = "fetchProperties", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/response/UserAttributesResponse$Privileges;getTelemetry()Z", remap = false), remap = false, require = 0)
    private boolean getTelemetry(UserAttributesResponse.Privileges instance) {
        return false;
    }
}
