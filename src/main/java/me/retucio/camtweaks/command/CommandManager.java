package me.retucio.camtweaks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.camtweaks.command.commands.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

// registro de comandos
public class CommandManager {

    private String prefix = "$";

    public static final List<Command> commands = new ArrayList<>();
    public static final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public CommandManager() {
        registerCommands();
    }

    public static void registerCommands() {
        addCommand(new BindCommand());
        addCommand(new ToggleCommand());
        addCommand(new SendCommand());
        addCommand(new PrefixCommand());
        commands.sort(Comparator.comparing(Command::getName));
    }

    public static void addCommand(Command command) {
        commands.removeIf(existing -> existing.getName().equals(command.getName()));
        command.registerTo(dispatcher);
        commands.add(command);
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        dispatcher.execute(message, mc.getNetworkHandler().getCommandSource());
    }

    public static Command getCommandByName(String name) {
        for (Command command : commands)
            if (command.getName().equals(name))
                return command;
        return null;
    }

    public <T extends Command> T getCommandByClass(Class<T> clazz) {
        for (Command command : commands)
            if (clazz.isInstance(command))
                return clazz.cast(command);
        return null;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
