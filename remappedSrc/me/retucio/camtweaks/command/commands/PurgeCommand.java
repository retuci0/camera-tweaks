package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.util.interfaces.IChatHud;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.command.CommandSource;

import java.util.List;

public class PurgeCommand extends Command {

    public PurgeCommand() {
        super("purgar", "elimina mensajes del chat", "purge");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("cantidad", IntegerArgumentType.integer(1))
                .executes(context -> {
                    int amount = IntegerArgumentType.getInteger(context, "cantidad");
                    purgeMessages(amount);
                    return 1;
                })
        );
    }

    private void purgeMessages(int amount) {
        ChatHud chatHud = mc.inGameHud.getChatHud();

        synchronized (chatHud) {
            List<ChatHudLine.Visible> visibleMessages = ((IChatHud) chatHud).smegma$getVisibleMessages();
            int toRemove = Math.min(amount, visibleMessages.size());
            for (int i = 0; i < toRemove; i++)
                visibleMessages.removeFirst();
        }
    }
}
