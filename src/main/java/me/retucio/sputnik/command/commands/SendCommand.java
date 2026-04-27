package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.mixin.accessors.ClientPacketListenerAccessor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.util.Crypt;

import java.time.Instant;

// uso principal: protección de coordenadas de ChatPlus
public class SendCommand extends Command {

    public SendCommand() {
        super("enviar", "manda un mensaje en el chat", "send");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.then(argument("message", StringArgumentType.greedyString()).executes(context -> {
            String message = context.getArgument("message", String.class);

            if (message != null && !message.isEmpty()) {
                Instant instant = Instant.now();
                long salt = Crypt.SaltSupplier.getLong();
                ClientPacketListener handler = mc.getConnection();

                // obtener últimos mensajes vistos para la firma
                ClientPacketListenerAccessor listener = ((ClientPacketListenerAccessor) handler);
                if (listener == null) return 0;
                LastSeenMessagesTracker tracker = ((ClientPacketListenerAccessor) handler).getLastSeenMessagesTracker();
                LastSeenMessagesTracker.Update lastSeenMessages = tracker.generateAndApplyUpdate();

                // empacar firma para un chat seguro
                MessageSignature messageSignature = ((ClientPacketListenerAccessor) handler)
                        .getSignedMessageEncoder()
                        .pack(new SignedMessageBody(
                                message, instant, salt, lastSeenMessages.lastSeen()
                        )
                );

                // enviar paquete para enviar mensaje
                handler.send(new ServerboundChatPacket(
                        message,
                        instant,
                        salt,
                        messageSignature,
                        lastSeenMessages.update()
                ));
            }

            return SUCCESS;
        }));
    }
}