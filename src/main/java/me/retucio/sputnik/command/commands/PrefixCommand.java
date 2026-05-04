package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

// también se puede hacer desde los ajustes de la interfaz
public class PrefixCommand extends Command {

    public PrefixCommand() {
        super("prefijo", "cambia el prefijo de los comandos", "prefix");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder
            .then(argument("prefijo", StringArgumentType.word()).executes(ctx -> {
                String prefix = ctx.getArgument("prefijo", String.class);
                ClientSettingsPanel.clientSettings.commandPrefix.setValue(prefix);
                ChatUtil.info(Component.literal("prefijo cambiado a " + ChatFormatting.AQUA + prefix));
                return SUCCESS;
            }));
    }
}
