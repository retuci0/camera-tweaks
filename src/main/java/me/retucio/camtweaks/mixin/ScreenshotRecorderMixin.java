package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.misc.ScreenshotPlus;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {

    // "method_22691" es la lambda de la función "saveScreenshot()" (por lo menos en 1.21.6)
    @Inject(method = "method_22691", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"), cancellable = true)
    private static void sendActionsMessage(NativeImage nativeImage, File file, Consumer<Text> consumer, CallbackInfo ci) {
        ScreenshotPlus screenshotPlus = getScreenshotPlus();
        if (screenshotPlus.isEnabled()) ci.cancel();
    }

    @Inject(method = "method_22691", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;writeTo(Ljava/io/File;)V"), cancellable = true)
    private static void interceptScreenshotFile(NativeImage nativeImage, File file, Consumer<Text> consumer, CallbackInfo ci) throws IOException {
        ScreenshotPlus screenshotPlus = getScreenshotPlus();

        // aunque el módulo esté apagado, pasar las referencias para poder usar los comandos para las capturas
        screenshotPlus.setScreenshot(nativeImage);
        screenshotPlus.setScreenshotFile(file);

        if (!screenshotPlus.isEnabled()) return;

        // por los threads o lo que sea
        mc.execute(screenshotPlus::sendScreenshotMessage);

        switch (screenshotPlus.defaultAction.getValue()) {
            case COPY -> screenshotPlus.copyScreenshot(nativeImage);
            case SAVE -> screenshotPlus.saveScreenshot();
            case NONE -> ci.cancel();
        }

        ci.cancel();
    }

    @Unique
    private static ScreenshotPlus getScreenshotPlus() {
        return ModuleManager.INSTANCE.getModuleByClass(ScreenshotPlus.class);
    }
}
