package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class GarbageCleanerCommand extends Command {

    public GarbageCleanerCommand() {
        super("cleanram", "limpia la ram mediante el recolector de basura (gc)", "gc", "garbagecollect");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            System.gc();
            ChatUtil.info(Text.of("ram limpiada"));
            return SUCCESS;
        });
    }
}