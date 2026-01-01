package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// también se puede hacer desde los ajustes de la interfaz
public class PrefixCommand extends Command {

    public PrefixCommand() {
        super("prefijo", "cambia el prefijo de los comandos", "prefix");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
            .then(argument("prefijo", StringArgumentType.word()).executes(ctx -> {
                String prefix = ctx.getArgument("prefijo", String.class);
                ClientSettingsFrame.guiSettings.commandPrefix.setValue(prefix);
                ChatUtil.info(Text.literal("prefijo cambiado a " + Formatting.AQUA + prefix));
                return SUCCESS;
            }));
    }
}
