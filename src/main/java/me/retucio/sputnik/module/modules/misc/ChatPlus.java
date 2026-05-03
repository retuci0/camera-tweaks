package me.retucio.sputnik.module.modules.misc;

import com.github.retucio.neutrino.EventListener;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.event.input.ClientClickEvent;
import me.retucio.sputnik.event.network.ReceiveMessageEvent;
import me.retucio.sputnik.event.network.SendMessageEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.interfaces.IGuiMessage;
import me.retucio.sputnik.util.interfaces.IGuiMessageLine;
import me.retucio.sputnik.util.interfaces.TextVisitor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** continúa en:
 * @see ChatComponentMixin
 * @see ChatScreenMixin
 * @see DrawingBackgroundGraphicsAccessMixin
 * @see DrawingFocusedGraphicsAccessMixin
 * @see GuiMixin
 * @see StringUtilMixin
 *
 * @author retucio
 */

public class ChatPlus extends Module {

    // ajustes
    SettingGroup sgChat = addSg(new SettingGroup("funcionamiento del chat", true));

    private final BooleanSetting timestamps = sgGeneral.add(new BooleanSetting("sello de tiempo", "muestra a qué hora se ha enviado un mensaje", true));
    private final BooleanSetting timestampSecs = sgGeneral.add(new BooleanSetting("mostrar segundos", "muestra segundos también en el sello de tiempo", false));
    public final BooleanSetting showHeads = sgGeneral.add(new BooleanSetting("cabezas", "muestra la cabeza del jugador junto a su mensaje", true));

    private final BooleanSetting coordsProtection = sgChat.add(new BooleanSetting("proteger coordenadas", "evitar enviar coordenadas por el chat", true));
    public final BooleanSetting keepHistory = sgChat.add(new BooleanSetting("no borrar chat", "no borrar el chat tras desconectarse", true));
    private final BooleanSetting logger = sgChat.add(new BooleanSetting("registro", "evita que se borre el chat de un server", true));
    public final BooleanSetting noCharLimit = sgChat.add(new BooleanSetting("quitar límite de caracteres", "te deja escribir mensajes tan largos como desees", false));
    public final NumberSetting chatHistoryExtraLength = sgChat.add(new NumberSetting("expandir chat", "cuántas líneas añadir al historial del chat",
            0, 0, 1000, 1));

    public ChatPlus()  {
        super("chat plus",
                "mejoras para el chat",
                Category.MISC);
        updateDateFormat();
        updateClientName();
        timestamps.onUpdate(timestampSecs::visibility);
        timestampSecs.onUpdate(v -> updateDateFormat());
    }

    public final IntList lines = new IntArrayList();
    public GuiMessage.Line line;

    private record CustomHeadEntry(String prefix, Identifier texture) {}
    private static final List<CustomHeadEntry> CUSTOM_HEAD_ENTRIES = new ArrayList<>();
    private SimpleDateFormat dateFormat;

    private static final Pattern LOGGER_REGEX = Pattern.compile("\\n([\\n\\s])+\\n");
    private static final Pattern USERNAME_REGEX = Pattern.compile("^(?:\\[[0-9]{2}:[0-9]{2}]\\s*)?(?:<([^<>\\s]+)>|([^<>\\s]+)).*");
    private static final Pattern TIMESTAMP_REGEX = Pattern.compile("^<\\d{1,2}:\\d{1,2}>");
    private static final Pattern COORDS_REGEX = Pattern.compile("(?<x>-?\\d{3,}(?:\\.\\d*)?)(?:\\s+(?<y>-?\\d{1,3}(?:\\.\\d*)?))?\\s+(?<z>-?\\d{3,}(?:\\.\\d*)?)");

    @EventListener
    private void onReceiveMessage(ReceiveMessageEvent event) {
        Component message = event.getMessage();

        // registrar mensajes para evitar su eliminación
        if (logger.getValue()) {
            String messageString = message.getString();
            if (LOGGER_REGEX.matcher(messageString).find()) {
                MutableComponent newMessage = Component.empty();
                TextVisitor.visit(message, (text, style, string) -> {
                    Matcher antiClearMatcher = LOGGER_REGEX.matcher(string);
                    if (antiClearMatcher.find())
                        newMessage.append(Component.literal(antiClearMatcher.replaceAll("\n\n")).setStyle(style));
                    else
                        newMessage.append(text.plainCopy().setStyle(style));

                    return Optional.empty();
                }, Style.EMPTY);
                message = newMessage;
            }
        }

        // agregar sellos de tiempo a los mensajes
        if (timestamps.getValue()) {
            Component timestamp = Component.literal("[" + dateFormat.format(new Date()) + "] ").withStyle(ChatFormatting.GRAY);
            message = Component.empty().append(timestamp).append(message);
        }

        // modificar el mensaje final
        event.setMessage(message);
    }

    @EventListener
    private void onSendMessage(SendMessageEvent event) {
        // evitar mandar coordenadas por el chat
        if (coordsProtection.getValue() && containsCoordinates(event.getMessage())) {
            ChatUtil.warn(Component.literal("cuidadito con las coordenadas chavalín").append(
                    getSendButton(event.getMessage())));

            event.cancel();
        }
    }

    public void updateClientName() {
        CUSTOM_HEAD_ENTRIES.add(new CustomHeadEntry(ChatUtil.getPrefixNoFormatting(), Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "icon_chat.png")));  // no funciona con formato de colores
        CUSTOM_HEAD_ENTRIES.add(new CustomHeadEntry("[Debug]", Identifier.fromNamespaceAndPath(Sputnik.MOD_ID, "icon_mc.png")));
    }

    @SuppressWarnings("DataFlowIssue")
    public void beforeDrawMessage(GuiGraphicsExtractor context, int y, int color) {
        if (!isEnabled() || !showHeads.getValue() || lines == null) return;

        if (((IGuiMessageLine) (Object) line).sputnik$isStartOfEntry())  {
            drawTexture(context, (IGuiMessage) (Object) line, y, color);
        }
    }

    public void afterDrawMessage() {
        if (!isEnabled() || !showHeads.getValue()) return;
        line = null;
    }

    @SuppressWarnings("DataFlowIssue")
    private void drawTexture(GuiGraphicsExtractor gui, IGuiMessage line, int y, int color) {
        String text = line.sputnik$getText().trim();

        int startOffset = 0;

        try {
            Matcher m = TIMESTAMP_REGEX.matcher(text);
            if (m.find()) startOffset = m.end() + 1;
        }
        catch (IllegalStateException ignored) {}

        for (CustomHeadEntry entry : CUSTOM_HEAD_ENTRIES) {
            if (text.startsWith(entry.prefix(), startOffset)) {
                gui.blit(RenderPipelines.GUI_TEXTURED, entry.texture(), 0, y, 0, 0, 8, 8, 64, 64, 64, 64, color);
                return;
            }
        }

        GameProfile sender = getSender(line, text);
        if (sender == null) return;

        PlayerInfo entry = mc.getConnection().getPlayerInfo(sender.id());
        if (entry == null) return;

        PlayerFaceExtractor.extractRenderState(gui, entry.getSkin(), 0, y, 8, color);
    }

    @SuppressWarnings("DataFlowIssue")
    private GameProfile getSender(IGuiMessage line, String text) {
        // obtener el jugador que envió un mensaje
        GameProfile sender = line.sputnik$getSender();

        if (sender == null) {
            Matcher usernameMatcher = USERNAME_REGEX.matcher(text);

            if (usernameMatcher.matches()) {
                String username = usernameMatcher.group(1);
                if (username == null)
                    username = usernameMatcher.group(2);

                PlayerInfo entry = mc.getConnection().getPlayerInfo(username);
                if (entry != null) sender = entry.getProfile();
            }
        }
        return sender;
    }

    @SuppressWarnings("DataFlowIssue")
    private MutableComponent getSendButton(String message) {
        // botón para enviar mensaje con coordenadas de todos modos
        MutableComponent sendButton = Component.literal("\n[ME LA SUDA]");
        MutableComponent hintBaseText = Component.literal("");

        MutableComponent hintMsg = Component.literal("enviar de todos modos:");
        hintMsg.setStyle(hintBaseText.getStyle().applyFormat(ChatFormatting.GRAY));
        hintBaseText.append(hintMsg);

        hintBaseText.append(Component.literal("\n" + message));

        sendButton.setStyle(sendButton.getStyle()
                .applyFormat(ChatFormatting.DARK_RED)
                .withClickEvent(new ClientClickEvent(CommandManager.getCommandByName("send").toString(message)))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return sendButton;
    }

    public void removeLine(int index) {
        lines.removeInt(index);
    }

    private void updateDateFormat() {
        dateFormat = new SimpleDateFormat(timestampSecs.getValue() ? "HH:mm:ss" : "HH:mm");
    }

    private boolean containsCoordinates(String message) {
        return COORDS_REGEX.matcher(message).find();
    }
}