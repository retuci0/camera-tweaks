package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.misc.ScreenshotPlus;
import net.minecraft.command.CommandSource;

// se usa principalmente para el módulo ScreenshotPlus, pero también se puede usar sin él supongo
public class CopyScreenshotCommand extends Command {

    public CopyScreenshotCommand() {
        super("copiarcaptura", "copia la captura más reciente al portapapeles");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            ScreenshotPlus screenshotPlus = ModuleManager.INSTANCE.getModuleByClass(ScreenshotPlus.class);

            if (screenshotPlus.getScreenshot() != null) mc.execute(screenshotPlus::copyScreenshot);

            return SUCCESS;
        });
    }
}