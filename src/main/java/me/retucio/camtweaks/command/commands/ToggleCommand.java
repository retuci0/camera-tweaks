package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.command.args.ModuleArgumentType;
import me.retucio.camtweaks.module.Module;
import net.minecraft.command.CommandSource;

import static me.retucio.camtweaks.CameraTweaks.moduleManager;

// porque usar el ratón es más lento que escribir, a menos que tengas toque con el teclado como yo
public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("alternar", "enciende / apaga módulos", "toggle");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(literal("todos")
                        .then(literal("on")
                                .executes(ctx -> {
                                    moduleManager.getModules().forEach(module -> {
                                        if (!module.isEnabled()) module.toggle();
                                    }); return SUCCESS;
                                })
                        )
                        .then(literal("off")
                                .executes(ctx -> {
                                    moduleManager.getEnabledModules().forEach(Module::toggle);
                                    return SUCCESS;
                                })
                        )
                )
                .then(argument("módulo", ModuleArgumentType.INSTANCE)
                        .executes(ctx -> {
                            Module module = ctx.getArgument("módulo", Module.class);
                            module.toggle();
                            return SUCCESS;
                        })
                        .then(literal("on")
                                .executes(ctx -> {
                                    Module module = ctx.getArgument("módulo", Module.class);
                                    module.setEnabled(true);
                                    return SUCCESS;
                                })
                        )
                        .then(literal("off")
                                .executes(ctx -> {
                                    Module module = ctx.getArgument("módulo", Module.class);
                                    module.setEnabled(false);
                                    return SUCCESS;
                                })
                        )
                );
    }
}
