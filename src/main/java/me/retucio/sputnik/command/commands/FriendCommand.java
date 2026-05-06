package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.command.args.PlayerArgumentType;
import me.retucio.sputnik.friend.Friend;
import me.retucio.sputnik.friend.FriendManager;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.player.Player;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", "gestión de amigos", "amigo", "f");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder
                .then(literal("add")
                        .then(argument("amigo", PlayerArgumentType.INSTANCE)
                                .executes(ctx -> {
                                        Player friend = ctx.getArgument("amigo", Player.class);
                                        FriendManager.INSTANCE.add(friend.getUUID());
                                        ChatUtil.info(ChatFormatting.GREEN + friend.getName().getString() + ChatFormatting.RESET + " ahora es tu amigo :)");
                                        return SUCCESS;
                                })
                        )
                )
                .then(literal("remove")
                        .then(argument("amigo", PlayerArgumentType.INSTANCE)
                                .executes(ctx -> {
                                        Player friend = ctx.getArgument("amigo", Player.class);
                                        FriendManager.INSTANCE.remove(friend.getUUID());
                                        ChatUtil.info(ChatFormatting.GREEN + friend.getName().getString() + ChatFormatting.RESET + " ya no es tu amigo :(");
                                        return SUCCESS;
                                })
                        )
                )
                .then(literal("list")
                        .executes(ctx -> {
                                ChatUtil.info(ChatFormatting.GOLD + "amigos:");
                                for (Friend friend : FriendManager.INSTANCE.getFriends()) {
                                    ChatUtil.info("- " + friend.getName());
                                }
                                return SUCCESS;
                        })
                );
    }
}
