package me.retucio.camtweaks.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.retucio.camtweaks.CameraTweaks.mc;

// para autocompletar nombres de jugadores
public class PlayerArgumentType implements ArgumentType<PlayerEntity> {

    public static final PlayerArgumentType INSTANCE = new PlayerArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(name -> Text.literal("Player with name " + name + " doesn't exist."));

    private static final Collection<String> EXAMPLES = List.of("retucio", "prepucio", "postpucio", "pucio", "adler hitdolf");

    private PlayerArgumentType() {}

    @Override
    public PlayerEntity parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        PlayerEntity playerEntity = null;

        for (PlayerEntity p : mc.world.getPlayers())
            if (p.getName().getString().equalsIgnoreCase(argument)) {
                playerEntity = p;
                break;
            }

        if (playerEntity == null) throw NO_SUCH_PLAYER.create(argument);

        return playerEntity;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(mc.world.getPlayers().stream().map(
                abstractClientPlayerEntity -> abstractClientPlayerEntity.getName().getString()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static PlayerEntity get(CommandContext<?> context) {
        return context.getArgument("jugador", PlayerEntity.class);
    }

}