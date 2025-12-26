package me.retucio.camtweaks.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.retucio.camtweaks.util.MiscUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

// para autocompletar entidades
import static me.retucio.camtweaks.CameraTweaks.mc;

public class EntityArgumentType implements ArgumentType<Entity> {

    public static final EntityArgumentType INSTANCE = new EntityArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_ENTITY = new DynamicCommandExceptionType(
            name -> Text.literal("Entidad \"" + name + "\" no encontrada"));

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

        if (mc.world == null)
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
        PlayerEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            double distance = mc.player.squaredDistanceTo(player);
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
        List<PlayerEntity> players = new ArrayList<>(mc.world.getPlayers());
        if (players.isEmpty()) throw NO_SUCH_ENTITY.create("@r");

        players.remove(mc.player);
        if (players.isEmpty()) return mc.player;

        Random random = new Random();
        return players.get(random.nextInt(players.size()));
    }

    private Entity parseEntitySelector(String selector) throws CommandSyntaxException {
        if (selector.equals("@e")) {
            List<Entity> entities = new ArrayList<>();
            for (Entity entity : mc.world.getEntities())
                if (!(entity instanceof PlayerEntity))
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

        for (Entity entity : mc.world.getEntities()) {
            String entityType = entity.getType().getTranslationKey();
            if (entityType.contains(type.toLowerCase().replace("minecraft:", "")))
                matchingEntities.add(entity);
        }

        if (matchingEntities.isEmpty()) throw NO_SUCH_ENTITY.create(type);

        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : matchingEntities) {
            double distance = mc.player.squaredDistanceTo(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    private Entity findEntityByUUID(UUID uuid) throws CommandSyntaxException {
        for (Entity entity : mc.world.getEntities())
            if (entity.getUuid().equals(uuid))
                return entity;

        throw NO_SUCH_ENTITY.create(uuid.toString());
    }

    private PlayerEntity findPlayerByName(String name) throws CommandSyntaxException {
        for (PlayerEntity player : mc.world.getPlayers())
            if (player.getName().getString().equalsIgnoreCase(name))
                return player;

        throw NO_SUCH_ENTITY.create(name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (mc.world != null)
            for (PlayerEntity player : mc.world.getPlayers())
                builder.suggest(player.getName().getString());


        builder.suggest("@p");
        builder.suggest("@r");
        builder.suggest("@s");

        Entity lookingAt = MiscUtil.getEntityPlayerIsLookingAt();
        if (lookingAt != null)
            builder.suggest(lookingAt.getUuid().toString());

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