package me.retucio.camtweaks.util;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.camtweaks.LoadModuleManagerEvent;
import me.retucio.camtweaks.ui.widgets.frames.settings.ClientSettingsFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// cosas útiles relacionadas al chat
public class ChatUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static String prefix = getDefaultPrefix();

    @SubscribeEvent
    public static void onLoadModuleManager(LoadModuleManagerEvent event) {
        prefix = Colors.getFormatting(Colors.mainColor) + "[" + ClientSettingsFrame.guiSettings.chatName.getValue() + "] ";
    }


    public static void addMessage(String text) {
        addMessage(Text.of(text));
    }

    public static void addMessage(Text text) {
        if (mc.inGameHud == null || !CameraTweaks.settingsApplied) return;
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
        return Colors.getFormatting(Colors.mainColor) + "[smegma] ";
    }
}
