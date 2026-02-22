package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.command.CommandSource;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RepeatWaitCommand extends Command {

    public RepeatWaitCommand() {
        super("repesperar", "combinación de $repetir y $esperar");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("ms", IntegerArgumentType.integer(1))
                .then(argument("veces", IntegerArgumentType.integer(1))
                        .then(argument("comando", StringArgumentType.greedyString())
                                .executes((context) -> {
                                    int ms = context.getArgument("ms", Integer.class);
                                    int times = context.getArgument("veces", Integer.class);
                                    String cmd = context.getArgument("comando", String.class);

                                    for (int i = 0; i < times; i++) {
                                        int delay = i * ms;
                                        int finalI = i;
                                        new ScheduledThreadPoolExecutor(2).schedule(
                                                () -> ChatUtil.info(
                                                        cmd.replaceAll("(?i)%index%",
                                                                String.valueOf(finalI))),
                                                delay,
                                                TimeUnit.MILLISECONDS
                                        );
                                    }
                                    return SUCCESS;
                                })
                        )
                )
        );
    }
}
