package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.command.CommandSource;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WaitCommand extends Command {

    private static final ScheduledExecutorService DELAYED_EXECUTOR = new ScheduledThreadPoolExecutor(2);

    public WaitCommand() {
        super("wait", "espera un tiempo determinado antes de ejecutar otro comando", "esperar", "sleep", "delay");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(argument("delay", IntegerArgumentType.integer(0))
                    .then(argument("comando", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            int delay = ctx.getArgument("delay", Integer.class);
                            String command = ctx.getArgument("comando", String.class);

                            DELAYED_EXECUTOR.schedule(() -> ChatUtil.simulateChatMessage(command), delay, TimeUnit.MILLISECONDS);
                            return SUCCESS;
                       })
                    )
                );
    }
}
