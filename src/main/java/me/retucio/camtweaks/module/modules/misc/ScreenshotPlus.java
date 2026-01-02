package me.retucio.camtweaks.module.modules.misc;

import me.retucio.camtweaks.command.CommandManager;
import me.retucio.camtweaks.event.events.ClientClickEvent;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/** continúa en:
 * @see me.retucio.camtweaks.mixin.ScreenshotRecorderMixin
 */

public class ScreenshotPlus extends Module {

    public BooleanSetting saveButton = addSetting(new BooleanSetting("botón de guardar", "mostrar botón para guardar la captura localmente", true));
    public BooleanSetting copyButton = addSetting(new BooleanSetting("botón de copiar", "mostrar botón para copiar la captura al portapapeles", true));
    public BooleanSetting openButton = addSetting(new BooleanSetting("botón de abrir", "mostrar botón para abrir el archivo de la captura", true));
    public BooleanSetting discardButton = addSetting(new BooleanSetting("botón de descartar", "mostrar botón para descartar la captura", true));

    public EnumSetting<ScreenshotActions> defaultAction = addSetting(new EnumSetting<>("por defecto", "qué acción tomar por defecto",
            ScreenshotActions.class, ScreenshotActions.NONE));

    private NativeImage screenshot;
    private File screenshotFile;

    public ScreenshotPlus() {
        super("capturas de pantalla",
                "elige qué hacer tras tomar una captura de pantalla",
                Category.MISC);
    }

    public void sendScreenshotMessage() {
        MutableText baseText = Text.literal("captura de pantalla tomada\n");
        if (saveButton.isEnabled() && !defaultAction.is(ScreenshotActions.SAVE)) baseText.append(getSaveButton().append(" "));
        if (copyButton.isEnabled()) baseText.append(getCopyButton().append(" "));
        if (openButton.isEnabled() && defaultAction.is(ScreenshotActions.SAVE)) baseText.append(getOpenButton().append(" "));
        if (discardButton.isEnabled() && !defaultAction.is(ScreenshotActions.NONE)) baseText.append(getDiscardButton().append(" "));
        ChatUtil.info(baseText);
    }


    // acciones con la captura

    public void copyScreenshot(NativeImage nativeImage) {
        try {
            BufferedImage bufferedImage = new BufferedImage(nativeImage.getWidth(), nativeImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < nativeImage.getWidth(); x++)
                for (int y = 0; y < nativeImage.getHeight(); y++)
                    bufferedImage.setRGB(x, y, nativeImage.getColorArgb(x, y));

            ScreenshotPlus.TransferableImage trans = new TransferableImage(bufferedImage);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
            ChatUtil.info("captura copiada al portapapeles");
        } catch (Exception e) {
            ChatUtil.error("no se pudo copiar la captura al portapapeles");
            e.printStackTrace();
        }
    }

    public void copyScreenshot() {
        copyScreenshot(this.screenshot);
    }

    public void saveScreenshot(NativeImage image) throws IOException {
        image.writeTo(screenshotFile);
        ChatUtil.info(Text.literal("captura guardada como: ").append(
                Text.literal(screenshotFile.getName())
                        .formatted(Formatting.UNDERLINE)
                        .styled(style -> style.withClickEvent(new ClickEvent.OpenFile(screenshotFile.getAbsoluteFile())))
                )
        );
    }

    public void saveScreenshot() throws IOException {
        saveScreenshot(this.screenshot);
    }

    // clase ayudante para el portapapeles
    public static class TransferableImage implements Transferable {
        private final Image image;

        public TransferableImage(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!DataFlavor.imageFlavor.equals(flavor))
                throw new UnsupportedFlavorException(flavor);

            return image;
        }
    }


    // botones para el mensaje

    @SuppressWarnings("DataFlowIssue")
    private MutableText getSaveButton() {
        MutableText button = Text.literal("[GUARDAR]");
        MutableText hintBaseText = Text.literal("");

        MutableText hintMsg = Text.literal(ScreenshotActions.SAVE.toString());
        hintMsg.setStyle(hintBaseText.getStyle().withFormatting(Formatting.GRAY));
        hintBaseText.append(hintMsg);

        button.setStyle(button.getStyle()
                .withFormatting(Formatting.BOLD, Formatting.GOLD)
                .withClickEvent(new ClientClickEvent(CommandManager.getCommandByName("guardarcaptura").toString()))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return button;
    }

    @SuppressWarnings("DataFlowIssue")
    private MutableText getCopyButton() {
        MutableText button = Text.literal("[COPIAR]");
        MutableText hintBaseText = Text.literal("");

        MutableText hintMsg = Text.literal(ScreenshotActions.COPY.toString());
        hintMsg.setStyle(hintBaseText.getStyle().withFormatting(Formatting.GRAY));
        hintBaseText.append(hintMsg);

        button.setStyle(button.getStyle()
                .withFormatting(Formatting.BOLD, Formatting.AQUA)
                .withClickEvent(new ClientClickEvent(CommandManager.getCommandByName("copiarcaptura").toString()))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return button;
    }

    private MutableText getOpenButton() {
        MutableText button = Text.literal("[ABRIR]");
        MutableText hintBaseText = Text.literal("");

        MutableText hintMsg = Text.literal("abrir archivo de imagen");
        hintMsg.setStyle(hintBaseText.getStyle().withFormatting(Formatting.GRAY));
        hintBaseText.append(hintMsg);

        button.setStyle(button.getStyle()
                .withFormatting(Formatting.BOLD, Formatting.GRAY)
                .withClickEvent(new ClickEvent.OpenFile(screenshotFile))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return button;
    }

    @SuppressWarnings("DataFlowIssue")
    private MutableText getDiscardButton() {
        MutableText button = Text.literal("[DESCARTAR]");
        MutableText hintBaseText = Text.literal("");

        MutableText hintMsg = Text.literal(ScreenshotActions.NONE.toString());
        hintMsg.setStyle(hintBaseText.getStyle().withFormatting(Formatting.GRAY));
        hintBaseText.append(hintMsg);

        button.setStyle(button.getStyle()
                .withFormatting(Formatting.BOLD, Formatting.DARK_RED)
                .withClickEvent(new ClientClickEvent(CommandManager.getCommandByName("purgar").toString("1")))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return button;
    }


    // otros

    public enum ScreenshotActions {
        SAVE("guardar archivo"),
        COPY("copiar al portapapeles"),
        NONE("no hacer nada");

        private final String name;
        ScreenshotActions(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    public NativeImage getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(NativeImage screenshot) {
        this.screenshot = screenshot;
    }

    public File getScreenshotFile() {
        return screenshotFile;
    }

    public void setScreenshotFile(File screenshotFile) {
        this.screenshotFile = screenshotFile;
    }
}
