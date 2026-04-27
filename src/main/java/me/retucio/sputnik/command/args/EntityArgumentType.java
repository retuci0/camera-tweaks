package me.retucio.sputnik.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.retucio.sputnik.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static me.retucio.sputnik.Sputnik.mc;


// para autocompletar entidades
public class EntityArgumentType implements ArgumentType<Entity> {

    public static final EntityArgumentType INSTANCE = new EntityArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_ENTITY = new DynamicCommandExceptionType(
            name -> Component.literal("Entidad \"" + name + "\" no encontrada"));

    private static final Collection<String> EXAMPLES = List.of(
            "AdlerHitdolf",
            "@p"
    );

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private EntityArgumentType() {}

    @Override
    public Entity parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();

        if (mc.level == null)
            throw NO_SUCH_ENTITY.create(argument);

        if (argument.startsWith("@"))
            return parseSelector(argument);

        if (isUUID(argument)) {
            UUID uuid = UUID.fromString(argument);
            return findEntityByUUID(uuid);
        }

        return findPlayerByName(argument);
    }

    private boolean isUUID(String input) {
        return UUID_PATTERN.matcher(input).matches();
    }

    private Entity parseSelector(String selector) throws CommandSyntaxException {
        if (mc.player == null) throw NO_SUCH_ENTITY.create(selector);

        return switch (selector.toLowerCase()) {
            case "@p" ->
                    findNearestPlayer();
            case "@r" ->
                    findRandomPlayer();
            case "@s" ->
                    mc.player;
            case "@a" -> throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument()
                    .createWithContext(new StringReader(selector));
            default -> {
                if (selector.startsWith("@e"))
                    yield parseEntitySelector(selector);
                 else if (selector.startsWith("@p") && selector.length() > 2)
                    yield parsePlayerSelector(selector);
                throw NO_SUCH_ENTITY.create(selector);
            }
        };
    }

    private Entity findNearestPlayer() throws CommandSyntaxException {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;

            double distance = mc.player.distanceToSqr(player);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }

        if (nearest == null)
            throw NO_SUCH_ENTITY.create("@p");

        return nearest;
    }

    private Entity findRandomPlayer() throws CommandSyntaxException {
        List<Player> players = new ArrayList<>(mc.level.players());
        if (players.isEmpty()) throw NO_SUCH_ENTITY.create("@r");

        players.remove(mc.player);
        if (players.isEmpty()) return mc.player;

        Random random = new Random();
        return players.get(random.nextInt(players.size()));
    }

    private Entity parseEntitySelector(String selector) throws CommandSyntaxException {
        if (selector.equals("@e")) {
            List<Entity> entities = new ArrayList<>();
            for (Entity entity : mc.level.getEntities().getAll())
                if (!(entity instanceof Player))
                    entities.add(entity);

            if (entities.isEmpty()) throw NO_SUCH_ENTITY.create(selector);

            Random random = new Random();
            return entities.get(random.nextInt(entities.size()));
        }

        if (selector.startsWith("@e[type=")) {
            String type = selector.substring(8, selector.length() - 1);
            return findEntityByType(type);
        }

        throw NO_SUCH_ENTITY.create(selector);
    }

    private Entity parsePlayerSelector(String selector) throws CommandSyntaxException {
        return findNearestPlayer();
    }

    private Entity findEntityByType(String type) throws CommandSyntaxException {
        List<Entity> matchingEntities = new ArrayList<>();

        for (Entity entity : mc.level.getEntities().getAll()) {
            String entityType = entity.getType().getDescriptionId();
            if (entityType.contains(type.toLowerCase().replace("minecraft:", "")))
                matchingEntities.add(entity);
        }

        if (matchingEntities.isEmpty()) throw NO_SUCH_ENTITY.create(type);

        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : matchingEntities) {
            double distance = mc.player.distanceToSqr(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    private Entity findEntityByUUID(UUID uuid) throws CommandSyntaxException {
        for (Entity entity : mc.level.getEntities().getAll())
            if (entity.getUUID().equals(uuid))
                return entity;

        throw NO_SUCH_ENTITY.create(uuid.toString());
    }

    private Player findPlayerByName(String name) throws CommandSyntaxException {
        for (Player player : mc.level.players())
            if (player.getName().getString().equalsIgnoreCase(name))
                return player;

        throw NO_SUCH_ENTITY.create(name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (mc.level != null)
            for (Player player : mc.level.players())
                builder.suggest(player.getName().getString());


        builder.suggest("@p");
        builder.suggest("@r");
        builder.suggest("@s");

        Entity lookingAt = EntityUtil.getEntityPlayerIsLookingAt();
        if (lookingAt != null)
            builder.suggest(lookingAt.getUUID().toString());

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static Entity get(CommandContext<?> context, String name) {
        return context.getArgument(name, Entity.class);
    }
}