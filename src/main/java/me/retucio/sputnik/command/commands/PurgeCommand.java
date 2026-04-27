package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.util.interfaces.IChatComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;

public class PurgeCommand extends Command {

    public PurgeCommand() {
        super("purgar", "elimina mensajes del chat", "purge");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.then(argument("cantidad", IntegerArgumentType.integer(1))
                .executes(context -> {
                    int amount = IntegerArgumentType.getInteger(context, "cantidad");
                    purgeMessages(amount);
                    return 1;
                })
        );
    }

    private void purgeMessages(int amount) {
        ChatComponent chatHud = mc.gui.getChat();

        synchronized (chatHud) {
            List<GuiMessage.Line> visibleMessages = ((IChatComponent) chatHud).sputnik$getVisibleMessages();
            int toRemove = Math.min(amount, visibleMessages.size());
            for (int i = 0; i < toRemove; i++)
                visibleMessages.removeFirst();
        }
    }
}
