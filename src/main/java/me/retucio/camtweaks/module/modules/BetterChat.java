package me.retucio.camtweaks.module.modules;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.event.Subscribe;
import me.retucio.camtweaks.event.events.ClientClickEvent;
import me.retucio.camtweaks.event.events.ReceiveMessageEvent;
import me.retucio.camtweaks.event.events.SendMessageEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.interfaces.IChatHudLine;
import me.retucio.camtweaks.util.interfaces.IChatHudLineVisible;
import me.retucio.camtweaks.util.interfaces.TextVisitor;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// continúa en ChatHudMixin, ChatScreenMixin, InGameHudMixin & StringHelperMixin
public class BetterChat extends Module {

    // ajustes
    public BooleanSetting timestamps = new BooleanSetting("sello de tiempo", "muestra a qué hora se ha enviado un mensaje", false);
    public BooleanSetting timestampSecs = new BooleanSetting("mostrar segundos", "muestra segundos también en el sello de tiempo", false);
    public BooleanSetting showHeads = new BooleanSetting("cabezas", "muestra la cabeza del jugador junto a su mensaje", true);
    public BooleanSetting coordsProtection = new BooleanSetting("proteger coordenadas", "evitar enviar coordenadas por el chat", true);

    public BooleanSetting keepHistory = new BooleanSetting("no borrar chat", "no borrar el chat tras desconectarse", true);
    public BooleanSetting logger = new BooleanSetting("registro", "evita que se borre el chat de un server", true);
    public BooleanSetting noCharLimit = new BooleanSetting("quitar límite de caracteres", "te deja escribir mensajes tan largos como desees", false);
    public NumberSetting chatHistoryExtraLength = new NumberSetting("expandir chat", "cuántas líneas añadir al historial del chat",
            0, 0, 1000, 1);

    public BetterChat()  {
        super("chat plus", "mejoras para el chat");
        addSettings(timestamps, timestampSecs, showHeads, coordsProtection, keepHistory, logger, noCharLimit, chatHistoryExtraLength);

        updateDateFormat();
        timestamps.onUpdate(b -> timestampSecs.setVisible(b));
        timestampSecs.onUpdate(x -> updateDateFormat());
    }

    public final IntList lines = new IntArrayList();
    private static final Pattern antiClearRegex = Pattern.compile("\\n(\\n|\\s)+\\n");
    private static final Pattern usernameRegex = Pattern.compile("^(?:<[0-9]{2}:[0-9]{2}>\\s)?<(.*?)>.*");

    private record CustomHeadEntry(String prefix, Identifier texture) {}
    private static final List<CustomHeadEntry> CUSTOM_HEAD_ENTRIES = new ArrayList<>();
    private static final Pattern TIMESTAMP_REGEX = Pattern.compile("^<\\d{1,2}:\\d{1,2}>");

    private SimpleDateFormat dateFormat;
    private void updateDateFormat() {
        dateFormat = new SimpleDateFormat(timestampSecs.isEnabled() ? "HH:mm:ss" : "HH:mm");
    }

    private static final Pattern coordRegex = Pattern.compile("(?<x>-?\\d{3,}(?:\\.\\d*)?)(?:\\s+(?<y>-?\\d{1,3}(?:\\.\\d*)?))?\\s+(?<z>-?\\d{3,}(?:\\.\\d*)?)");
    private boolean containsCoordinates(String message) {
        return coordRegex.matcher(message).find();
    }

    @Subscribe
    public void onReceiveMessage(ReceiveMessageEvent event) {
        Text message = event.getMessage();

        // registrar mensajes para evitar su eliminación
        if (logger.isEnabled()) {
            String messageString = message.getString();
            if (antiClearRegex.matcher(messageString).find()) {
                MutableText newMessage = Text.empty();
                TextVisitor.visit(message, (text, style, string) -> {
                    Matcher antiClearMatcher = antiClearRegex.matcher(string);
                    if (antiClearMatcher.find())
                        newMessage.append(Text.literal(antiClearMatcher.replaceAll("\n\n")).setStyle(style));
                    else
                        newMessage.append(text.copyContentOnly().setStyle(style));

                    return Optional.empty();
                }, Style.EMPTY);
                message = newMessage;
            }
        }

        // agregar sellos de tiempo a los mensajes
        if (timestamps.isEnabled()) {
            Text timestamp = Text.literal("<" + dateFormat.format(new Date()) + "> ").formatted(Formatting.GRAY);
            message = Text.empty().append(timestamp).append(message);
        }

        // modificar el mensaje final
        event.setMessage(message);
    }

    @Subscribe
    private void onSendMessage(SendMessageEvent event) {
        String message = event.getMessage();

        // evitar mandar coordenadas por el chat
        if (coordsProtection.isEnabled() && containsCoordinates(message)) {
            ChatUtil.warn(Text.literal("cuidadito con las coordenadas chavalín").append(
                    getSendButton(message)));

            event.cancel();
            return;
        }
        event.setMessage(message);
    }

    static {
        CUSTOM_HEAD_ENTRIES.add(new CustomHeadEntry("smegma", Identifier.of(CameraTweaks.MOD_ID, "icon_chat.png")));
    }

    public void beforeDrawMessage(DrawContext context, ChatHudLine.Visible line, int y, int color) {
        if (!isEnabled() || !showHeads.isEnabled()) return;

        // dibujar la cabeza al principio del mensaje
        if (((IChatHudLineVisible) (Object) line).smegma$isStartOfEntry())
            drawTexture(context, (IChatHudLine) (Object) line, y, color);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(10, 0);
    }

    public void afterDrawMessage(DrawContext context) {
        // finalizar renderizado de la cabeza
        if (!isEnabled() || !showHeads.isEnabled()) return;
        context.getMatrices().popMatrix();
    }

    private void drawTexture(DrawContext context, IChatHudLine line, int y, int color) {
        String text = line.smegma$getText().trim();

        int startOffset = 0;

        // buscar el sello del tiempo si lo hay para dibujar la cabeza correctamente
        try {
            Matcher m = TIMESTAMP_REGEX.matcher(text);
            if (m.find()) startOffset = m.end() + 1;
        } catch (IllegalStateException ignored) {}

        // dibujar el icono del mod
        for (CustomHeadEntry entry : CUSTOM_HEAD_ENTRIES) {
            if (text.startsWith(entry.prefix(), startOffset)) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, entry.texture(), 0, y, 0, 0, 8, 8, 64, 64, 64, 64, color);
                return;
            }
        }

        // obtener y dibujar la cabeza del jugador
        GameProfile sender = getSender(line, text);
        if (sender == null) return;

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(sender.getId());
        if (entry == null) return;

        PlayerSkinDrawer.draw(context, entry.getSkinTextures(), 0, y, 8);
    }

    // obtener el jugador que envió un mensaje
    private GameProfile getSender(IChatHudLine line, String text) {
        GameProfile sender = line.smegma$getSender();

        if (sender == null) {
            Matcher usernameMatcher = usernameRegex.matcher(text);

            if (usernameMatcher.matches()) {
                String username = usernameMatcher.group(1);

                PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(username);
                if (entry != null) sender = entry.getProfile();
            }
        }
        return sender;
    }

    // botón para enviar mensaje con coordenadas de todos modos
    private MutableText getSendButton(String message) {
        MutableText sendButton = Text.literal("[ME LA SUDA]");
        MutableText hintBaseText = Text.literal("");

        MutableText hintMsg = Text.literal("enviar de todos modos:");
        hintMsg.setStyle(hintBaseText.getStyle().withFormatting(Formatting.GRAY));
        hintBaseText.append(hintMsg);

        hintBaseText.append(Text.literal("\n" + message));

        sendButton.setStyle(sendButton.getStyle()
                .withFormatting(Formatting.DARK_RED)
                .withClickEvent(new ClientClickEvent(CommandManager.getCommandByName("send").toString(message)))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return sendButton;
    }

    public void removeLine(int index) {
        lines.removeInt(index);
    }
}