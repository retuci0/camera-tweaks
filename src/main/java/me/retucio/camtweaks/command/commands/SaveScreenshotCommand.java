package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.misc.ScreenshotPlus;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.command.CommandSource;

import java.io.IOException;

// se usa principalmente para el módulo ScreenshotPlus, pero también se puede usar sin él supongo
public class SaveScreenshotCommand extends Command {

    public SaveScreenshotCommand() {
        super("guardarcaptura", "guarda localmente la captura más reciente");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
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
