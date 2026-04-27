package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.command.args.ModuleArgumentType;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import net.minecraft.commands.SharedSuggestionProvider;
import me.retucio.sputnik.module.Module;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "abre el marco de ajustes de un módulo", "ajustes", "settings");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder
                .then(argument("módulo", ModuleArgumentType.INSTANCE)
                        .executes(ctx -> {
                            mc.execute(() -> openSf(ctx));
                            return SUCCESS;
                        })
                        .then(argument("x", IntegerArgumentType.integer())
                                .then(argument("y", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            mc.execute(() -> openSfWithPos(ctx,
                                                    ctx.getArgument("x", Integer.class),
                                                    ctx.getArgument("y", Integer.class)));
                                            return SUCCESS;
                                        })
                                )
                        )
                );
    }


    private void openSf(CommandContext<SharedSuggestionProvider> ctx) {
        Module module = ctx.getArgument("módulo", Module.class);
        mc.setScreen(ClickGUI.INSTANCE);

        if (ClickGUI.INSTANCE.isSettingsFrameOpen(module)) return;

        SettingsFrame sf = ClickGUI.INSTANCE.getSfOfModule(module);
        int x = mc.getWindow().getGuiScaledWidth() / 2 - sf.getW() / 2;
        int y = 67;

        ClickGUI.INSTANCE.openSettingsFrame(module, x, y);
    }

    private void openSfWithPos(CommandContext<SharedSuggestionProvider> ctx, int x, int y) {
        Module module = ctx.getArgument("módulo", Module.class);
        mc.setScreen(ClickGUI.INSTANCE);

        if (ClickGUI.INSTANCE.isSettingsFrameOpen(module)) {
            SettingsFrame sf = ClickGUI.INSTANCE.getSfOfModule(module);
            sf.setX(x);
            sf.setY(y);
        }

        ClickGUI.INSTANCE.openSettingsFrame(module, x, y);
    }
}
