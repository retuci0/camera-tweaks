package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.GetFOVEvent;
import me.retucio.camtweaks.event.events.RenderEvent;
import me.retucio.camtweaks.module.modules.Zoom;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Unique
    Zoom zoom = CameraTweaks.moduleManager.getModuleByClass(Zoom.class);

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float modifyFov(float original) {
        return EVENT_BUS.post(new GetFOVEvent(original)).getFov();
    }

//    @Inject(method = "render", at = @At("HEAD"))
//    private void onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
//        CameraTweaks.INSTANCE.EVENT_BUS.post(new RenderEvent());
//    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(float tickProgress, boolean sleeping, Matrix4f positionMatrix, CallbackInfo ci) {
        if (zoom.isEnabled() && !zoom.showHands.isEnabled())
            ci.cancel();
    }
}
