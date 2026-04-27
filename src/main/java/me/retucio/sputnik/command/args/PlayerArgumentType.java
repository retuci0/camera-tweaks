package me.retucio.sputnik.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.retucio.sputnik.Sputnik.mc;

// para autocompletar nombres de jugadores
public class PlayerArgumentType implements ArgumentType<Player> {

    public static final PlayerArgumentType INSTANCE = new PlayerArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(name -> Component.literal("Player with name " + name + " doesn't exist."));

    private static final Collection<String> EXAMPLES = List.of("retucio", "prepucio", "postpucio", "pucio", "adler hitdolf");

    private PlayerArgumentType() {}

    @Override
    public Player parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Player playerEntity = null;

        for (Player p : mc.level.players())
            if (p.getName().getString().equalsIgnoreCase(argument)) {
                playerEntity = p;
                break;
            }

        if (playerEntity == null) throw NO_SUCH_PLAYER.create(argument);

        return playerEntity;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(mc.level.players().stream().map(
                abstractClientPlayerEntity -> abstractClientPlayerEntity.getName().getString()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static Player get(CommandContext<?> context) {
        return context.getArgument("jugador", Player.class);
    }

}