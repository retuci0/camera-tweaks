package me.retucio.sputnik.mixin.mixins.io;

import com.mojang.blaze3d.platform.NativeImage;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ScreenshotPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;


@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {

    @Inject(method = "lambda$grab$1", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"), cancellable = true)
    private static void sendActionsMessage(NativeImage image, File file, Consumer<?> callback, CallbackInfo ci) {
        ScreenshotPlus screenshotPlus = getScreenshotPlus();
        if (screenshotPlus.isEnabled()) ci.cancel();
    }

    @Inject(method = "lambda$grab$1", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V"), cancellable = true)
    private static void interceptScreenshotFile(NativeImage image, File file, Consumer<?> callback, CallbackInfo ci) throws IOException {
        ScreenshotPlus screenshotPlus = getScreenshotPlus();

        // aunque el módulo esté apagado, pasar las referencias para poder usar los comandos para las capturas
        screenshotPlus.setScreenshot(image);
        screenshotPlus.setScreenshotFile(file);

        if (!screenshotPlus.isEnabled()) return;

        // por los threads o lo que sea
        Minecraft.getInstance().execute(screenshotPlus::sendScreenshotMessage);

        switch (screenshotPlus.defaultAction.getValue()) {
            case COPY -> screenshotPlus.copyScreenshot(image);
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
