package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class GarbageCleanerCommand extends Command {

    public GarbageCleanerCommand() {
        super("gc", "limpia la ram mediante el recolector de basura (gc)", "garbagecollect");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            System.gc();
            ChatUtil.info(Component.nullToEmpty("basura recolectada"));
            return SUCCESS;
        });
    }
}