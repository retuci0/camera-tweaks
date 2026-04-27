package me.retucio.sputnik.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.util.MiscUtil;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

// para autocompletar nombres de módulos (los espacios se cambian por guiones bajos)
public class ModuleArgumentType implements ArgumentType<Module> {

    public static final ModuleArgumentType INSTANCE = new ModuleArgumentType();

    private static final DynamicCommandExceptionType UNKNOWN_MODULE = new DynamicCommandExceptionType(
            name -> Component.literal("módulo \"" + name + "\" no encontrado"));

    private static final Collection<String> examples = ModuleManager.INSTANCE.getModules()
            .stream().limit(3).map(module -> module.getName().replace(" ", "_")).toList();

    @Override
    public Module parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString().replace("_", " ");
        Module module = ModuleManager.INSTANCE.getModuleByName(argument);
        if (module == null) throw UNKNOWN_MODULE.create(argument);
        return module;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ModuleManager.INSTANCE.getModules()
                .stream().map(module -> MiscUtil.removeAccentMarks(module.getName().replace(" ", "_"))), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return examples;
    }

    public static Module get(CommandContext<SharedSuggestionProvider> ctx) {
        return ctx.getArgument("módulo", Module.class);
    }
}
