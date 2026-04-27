package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.commands.SharedSuggestionProvider;

public class RepeatCommand extends Command {

    public RepeatCommand() {
        super("repetir", "repite un comando x veces", "repeat");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder
                .then(argument("veces", IntegerArgumentType.integer(1))
                        .then(argument("comando", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    int j = ctx.getArgument("veces", Integer.class);
                                    String command = ctx.getArgument("comando", String.class);
                                    for (int i = 0; i < j; i++) {
                                        ChatUtil.simulateChatMessage(command);
                                    }
                                    return SUCCESS;
                                })
                        )
                );
    }
}
