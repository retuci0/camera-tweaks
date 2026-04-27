package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.ScreenshotPlus;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.commands.SharedSuggestionProvider;

import java.io.IOException;

// se usa principalmente para el módulo ScreenshotPlus, pero también se puede usar sin él supongo
public class SaveScreenshotCommand extends Command {

    public SaveScreenshotCommand() {
        super("guardarcaptura", "guarda localmente la captura más reciente");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(ctx -> {
            ScreenshotPlus screenshotPlus = ModuleManager.INSTANCE.getModuleByClass(ScreenshotPlus.class);

            mc.execute(() -> {
                if (screenshotPlus.getScreenshot() != null) {
                    try {
                        screenshotPlus.saveScreenshot();
                    } catch (IOException e) {
                        ChatUtil.error("no se pudo guardar la captura");
                    }
                }
            });

            return SUCCESS;
        });
    }
}
