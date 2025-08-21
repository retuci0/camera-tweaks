package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class SendCommand extends Command {

    public SendCommand() {
        super("enviar", "manda un mensaje en el chat", "send");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(argument("mensaje", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            if (mc.player == null) return 0;
                            mc.player.sendMessage(Text.of(ctx.getArgument("mensaje", String.class)), false);
                            return SUCCESS;
                        })
        );
    }
}
