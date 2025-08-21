package me.retucio.camtweaks.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.retucio.camtweaks.module.Module;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static me.retucio.camtweaks.CameraTweaks.moduleManager;

public class ModuleArgumentType implements ArgumentType<Module> {

    public static final ModuleArgumentType INSTANCE = new ModuleArgumentType();
    private static final DynamicCommandExceptionType unknownModuleE = new DynamicCommandExceptionType(
            name -> Text.literal("módulo \"" + name + "\" no encontrado"));

    private static final Collection<String> examples = moduleManager.getModules()
            .stream().limit(3).map(Module::getName).toList();

    @Override
    public Module parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Module module = moduleManager.getModuleByName(argument);
        if (module == null) throw unknownModuleE.create(argument);
        return module;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(moduleManager.getModules().stream().map(Module::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return examples;
    }

    public static Module get(CommandContext<CommandSource> ctx) {
        return ctx.getArgument("módulo", Module.class);
    }
}
