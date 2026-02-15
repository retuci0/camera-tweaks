package me.retucio.sputnik.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.event.sputnik.LoadModuleManagerEvent;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// cosas útiles relacionadas al chat
public class ChatUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static String prefix = getDefaultPrefix();

    public static void onLoadModuleManager(LoadModuleManagerEvent event) {
        prefix = Colors.getFormatting(Colors.mainColor) + "[" + ClientSettingsFrame.guiSettings.chatName.getValue() + "] ";
    }

    public static void simulateChatMessage(String message) {
        if (message.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(message.substring(1));
        } else if (message.startsWith(CommandManager.INSTANCE.getPrefix())) {
            try {
                CommandManager.dispatch(message.substring(CommandManager.INSTANCE.getPrefix().length()));
            } catch (CommandSyntaxException e) {
                ChatUtil.error(e.getMessage());
            }
        } else {
            mc.player.networkHandler.sendChatMessage(message);
        }
    }


    public static void addMessage(String text) {
        addMessage(Text.of(text));
    }

    public static void addMessage(Text text) {
        if (mc.inGameHud == null || !Sputnik.settingsApplied) return;
        mc.inGameHud.getChatHud().addMessage(text);
    }

    public static void addMessageWithPrefix(String text) {
        addMessageWithPrefix(Text.of(text));
    }

    public static void addMessageWithPrefix(Text text) {
        addMessage(Text.literal(getPrefix() + Formatting.RESET).append(text));
    }

    public static void info(String text) {
        addMessage(Text.literal(getPrefix() + Formatting.RESET + text));
    }

    public static void info(Text text) {
        addMessage(Text.literal(getPrefix() + Formatting.RESET).append(text));
    }

    public static void warn(String text) {
        addMessage(Text.literal(getPrefix() + Formatting.YELLOW + text));
    }

    public static void warn(Text text) {
        addMessage(Text.literal(getPrefix() + Formatting.YELLOW).append(text));
    }

    public static void error(String text) {
        addMessage(Text.literal(getPrefix() + Formatting.RED + text));
    }

    public static void error(Text text) {
        addMessage(Text.literal(getPrefix() + Formatting.RED).append(text));
    }

    public static void sendServerMessage(String text) {
        sendServerMessage(Text.of(text));
    }

    public static void sendServerMessage(Text text) {
        if (mc.player == null) return;
        mc.player.sendMessage(text, false);
    }

    public static String getPrefix() {
        return prefix;
    }

    public static String getJustPrefix() {
        return ClientSettingsFrame.guiSettings.chatName.getValue();
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
