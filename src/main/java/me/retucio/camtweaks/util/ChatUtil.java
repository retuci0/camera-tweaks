package me.retucio.camtweaks.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// cosas útiles relacionadas al chat
public class ChatUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void addMessage(String text) {
        mc.inGameHud.getChatHud().addMessage(Text.of(text));
    }

    public static void addMessage(Text text) {
        mc.inGameHud.getChatHud().addMessage(text);
    }

    public static void addMessageWithPrefix(String text) {
        addMessageWithPrefix(Text.of(text));
    }

    public static void addMessageWithPrefix(Text text) {
        addMessage(Text.literal(Formatting.GREEN + "[smegma] " + Formatting.RESET).append(text));
    }

    public static void info(String text) {
        addMessage(Text.literal(Formatting.GREEN + "[smegma] " + Formatting.RESET + text));
    }

    public static void info(Text text) {
        addMessage(Text.literal(Formatting.GREEN + "[smegma] " + Formatting.RESET).append(text));
    }

    public static void warn(String text) {
        addMessage(Text.literal(Formatting.GREEN + "[smegma] " + Formatting.YELLOW + text));
    }

    public static void warn(Text text) {
        addMessage(Text.literal(Formatting.GREEN + "[smegma] " + Formatting.YELLOW).append(text));
    }

    public static void error(String text) {
        addMessage(Text.literal(Formatting.GREEN + "[smegma] " + Formatting.RED + text));
    }

    public static void error(Text text) {
        addMessage(Text.literal(Formatting.GREEN + "[smegma] " + Formatting.RED).append(text));
    }

    public static void sendServerMessage(String text) {
        sendServerMessage(Text.of(text));
    }

    public static void sendServerMessage(Text text) {
        if (mc.player == null) return;
        mc.player.sendMessage(text, false);
    }
}
