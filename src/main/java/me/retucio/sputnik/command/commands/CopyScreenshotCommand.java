package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ScreenshotPlus;
import net.minecraft.commands.SharedSuggestionProvider;

// se usa principalmente para el módulo ScreenshotPlus, pero también se puede usar sin él supongo
public class CopyScreenshotCommand extends Command {

    public CopyScreenshotCommand() {
        super("copiarcaptura", "copia la captura más reciente al portapapeles");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(ctx -> {
            ScreenshotPlus screenshotPlus = ModuleManager.INSTANCE.getModuleByClass(ScreenshotPlus.class);

            if (screenshotPlus.getScreenshot() != null) mc.execute(screenshotPlus::copyScreenshot);

            return SUCCESS;
        });
    }
}