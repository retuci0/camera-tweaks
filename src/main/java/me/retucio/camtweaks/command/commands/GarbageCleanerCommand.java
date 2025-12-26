package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class GarbageCleanerCommand extends Command {

    public GarbageCleanerCommand() {
        super("gc", "limpia la ram mediante el recolector de basura (gc)", "garbagecollect");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            System.gc();
            ChatUtil.info(Text.of("basura recolectada"));
            return SUCCESS;
        });
    }
}