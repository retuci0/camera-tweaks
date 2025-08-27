package me.retucio.camtweaks.util;

import me.retucio.camtweaks.ui.frames.ClickGUISettingsFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// cosas útiles relacionadas al chat
public class ChatUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static String prefix = Formatting.GREEN + "[" + ClickGUISettingsFrame.guiSettings.chatName.getValue() + "] ";

    public static void addMessage(String text) {
        if (mc.inGameHud == null) return;
        mc.inGameHud.getChatHud().addMessage(Text.of(text));
    }

    public static void addMessage(Text text) {
        if (mc.inGameHud == null) return;
        mc.inGameHud.getChatHud().addMessage(text);
    }

    public static void addMessageWithPrefix(String text) {
        addMessageWithPrefix(Text.of(text));
    }

    public static void addMessageWithPrefix(Text text) {
        addMessage(Text.literal(prefix + Formatting.RESET).append(text));
    }

    public static void info(String text) {
        addMessage(Text.literal(prefix + Formatting.RESET + text));
    }

    public static void info(Text text) {
        addMessage(Text.literal(prefix + Formatting.RESET).append(text));
    }

    public static void warn(String text) {
        addMessage(Text.literal(prefix + Formatting.YELLOW + text));
    }

    public static void warn(Text text) {
        addMessage(Text.literal(prefix + Formatting.YELLOW).append(text));
    }

    public static void error(String text) {
        addMessage(Text.literal(prefix + Formatting.RED + text));
    }

    public static void error(Text text) {
        addMessage(Text.literal(prefix + Formatting.RED).append(text));
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

    public static String getPrefixNoFormatting() {
        return "[" + ClickGUISettingsFrame.guiSettings.chatName.getValue() + "] ";
    }

    public static void updatePrefix(String newPrefix) {
        prefix = Formatting.GREEN + "[" + newPrefix + "] ";
    }
}
