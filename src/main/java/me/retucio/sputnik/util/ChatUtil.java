package me.retucio.sputnik.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.event.sputnik.LoadModuleManagerEvent;
import me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

// cosas útiles relacionadas al chat
public class ChatUtil {

    private static final Minecraft mc = Minecraft.getInstance();
    private static String prefix = getDefaultPrefix();

    public static void onLoadModuleManager(LoadModuleManagerEvent event) {
        prefix = Colors.getFormatting(Colors.mainColor) + "[" + ClientSettingsPanel.clientSettings.chatName.getValue() + "] ";
    }

    public static void simulateChatMessage(String message) {
        if (message.startsWith("/")) {
            mc.player.connection.sendCommand(message.substring(1));
        } else if (message.startsWith(CommandManager.INSTANCE.getPrefix())) {
            try {
                CommandManager.dispatch(message.substring(CommandManager.INSTANCE.getPrefix().length()));
            } catch (CommandSyntaxException e) {
                ChatUtil.error(e.getMessage());
            }
        } else {
            mc.player.connection.sendChat(message);
        }
    }


    public static void addMessage(String text) {
        addMessage(Component.nullToEmpty(text));
    }

    public static void addMessage(Component text) {
        if (!Sputnik.settingsApplied) return;
        mc.gui.getChat().addClientSystemMessage(text);
    }

    public static void addMessageWithPrefix(String text) {
        addMessageWithPrefix(Component.nullToEmpty(text));
    }

    public static void addMessageWithPrefix(Component text) {
        addMessage(Component.literal(getPrefix() + ChatFormatting.RESET).append(text));
    }

    public static void info(String text) {
        addMessage(Component.literal(getPrefix() + ChatFormatting.RESET + text));
    }

    public static void info(Component text) {
        addMessage(Component.literal(getPrefix() + ChatFormatting.RESET).append(text));
    }

    public static void warn(String text) {
        addMessage(Component.literal(getPrefix() + ChatFormatting.YELLOW + text));
    }

    public static void warn(Component text) {
        addMessage(Component.literal(getPrefix() + ChatFormatting.YELLOW).append(text));
    }

    public static void error(String text) {
        addMessage(Component.literal(getPrefix() + ChatFormatting.RED + text));
    }

    public static void error(Component text) {
        addMessage(Component.literal(getPrefix() + ChatFormatting.RED).append(text));
    }

    public static String getPrefix() {
        return prefix;
    }

    public static String getJustPrefix() {
        return ClientSettingsPanel.clientSettings.chatName.getValue();
    }

    public static String getPrefixNoFormatting() {
        return getPrefix().split(Colors.getFormatting(Colors.mainColor).toString())[1];
    }


    public static void updatePrefix(String newPrefix) {
        prefix = Colors.getFormatting(Colors.mainColor) + "[" + newPrefix + "] ";
    }

    public static String getDefaultPrefix() {
        return Colors.getFormatting(Colors.mainColor) + "[sputnik] ";
    }
}
