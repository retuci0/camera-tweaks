package me.retucio.sputnik.command;

import com.github.retucio.neutrino.EventListener;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.command.commands.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;

// registro de comandos
public class CommandManager {

    public static CommandManager INSTANCE;

    private String prefix = "$";

    public static final List<Command> commands = new ArrayList<>();
    public static final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public CommandManager() {
        registerCommands();
    }

    public static void registerCommands() {
        addCommand(new BindCommand());
        addCommand(new ClickSlotCommand());
        addCommand(new ConfigCommand());
        addCommand(new CopyScreenshotCommand());
        addCommand(new EnderChestCommand());
        addCommand(new GarbageCleanerCommand());
        addCommand(new PeekCommand());
        addCommand(new PluginsCommand());
        addCommand(new PrefixCommand());
        addCommand(new PurgeCommand());
        addCommand(new RepeatCommand());
        addCommand(new SaveScreenshotCommand());
        addCommand(new SendCommand());
        addCommand(new SpectateCommand());
        addCommand(new ToggleCommand());
        addCommand(new WaitCommand());

        commands.sort(Comparator.comparing(Command::getName));

        for (Command command : commands) {
            for (Method method : command.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(EventListener.class)) {
                    Sputnik.EVENT_BUS.subscribe(command);
                    break;
                }
            }
        }
    }

    public static void addCommand(Command command) {
        commands.removeIf(existing -> existing.getName().equals(command.getName()));
        command.registerTo(dispatcher);
        commands.add(command);
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        dispatcher.execute(message, Objects.requireNonNull(mc.getNetworkHandler()).getCommandSource());
    }

    public static Command getCommandByName(String name) {
        for (Command command : commands) {
            if (command.getName().equals(name)) return command;

            for (String alias : command.getAliases())
                if (alias.equals(name)) return command;
        }

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
