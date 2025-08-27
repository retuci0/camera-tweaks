package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.mixin.accessor.ClientPlayNetworkHandlerAccessor;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;

import java.time.Instant;

// uso principal: protección de coordenadas de ChatPlus, que ni siquiera funciona
public class SendCommand extends Command {

    public SendCommand() {
        super("enviar", "manda un mensaje en el chat", "send");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("message", StringArgumentType.greedyString()).executes(context -> {
            String message = context.getArgument("message", String.class);

            if (message != null && !message.isEmpty()) {
                Instant instant = Instant.now();
                long salt = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
                ClientPlayNetworkHandler handler = mc.getNetworkHandler();

                // obtener últimos mensajes vistos para la firma
                LastSeenMessagesCollector.LastSeenMessages lastSeenMessages =
                        ((ClientPlayNetworkHandlerAccessor) handler).getLastSeenMessagesCollector().collect();

                // empacar firma para un chat seguro
                MessageSignatureData messageSignatureData =
                        ((ClientPlayNetworkHandlerAccessor) handler).getMessagePacker().pack(
                                new MessageBody(message, instant, salt, lastSeenMessages.lastSeen())
                        );

                // enviar paquete para enviar mensaje
                handler.sendPacket(new ChatMessageC2SPacket(
                        message,
                        instant,
                        salt,
                        messageSignatureData,
                        lastSeenMessages.update()
                ));
            }

            return SUCCESS;
        }));
    }
}