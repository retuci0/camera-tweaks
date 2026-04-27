package me.retucio.sputnik.module.modules.misc;

import com.mojang.blaze3d.platform.NativeImage;
import me.retucio.sputnik.command.CommandManager;
import me.retucio.sputnik.event.input.ClientClickEvent;
import me.retucio.sputnik.mixin.mixins.io.ScreenshotMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/** continúa en:
 * @see ScreenshotMixin
 */

public class ScreenshotPlus extends Module {

    private final SettingGroup sgButtons = addSg(new SettingGroup("botones", true));

    public final EnumSetting<ScreenshotActions> defaultAction = sgGeneral.add(new EnumSetting<>("por defecto", "qué acción tomar por defecto",
            ScreenshotActions.class, ScreenshotActions.NONE));

    private final BooleanSetting saveButton = sgButtons.add(new BooleanSetting("botón de guardar", "mostrar botón para guardar la captura localmente", true));
    private final BooleanSetting copyButton = sgButtons.add(new BooleanSetting("botón de copiar", "mostrar botón para copiar la captura al portapapeles", true));
    private final BooleanSetting openButton = sgButtons.add(new BooleanSetting("botón de abrir", "mostrar botón para abrir el archivo de la captura", true));
    private final BooleanSetting discardButton = sgButtons.add(new BooleanSetting("botón de descartar", "mostrar botón para descartar la captura", true));

    private NativeImage screenshot;
    private File screenshotFile;

    public ScreenshotPlus() {
        super("capturas de pantalla",
                "elige qué hacer tras tomar una captura de pantalla",
                Category.MISC);
    }

    public void sendScreenshotMessage() {
        MutableComponent baseText = Component.literal("captura de pantalla tomada\n");
        if (saveButton.getValue() && !defaultAction.is(ScreenshotActions.SAVE)) baseText.append(getSaveButton().append(" "));
        if (copyButton.getValue()) baseText.append(getCopyButton().append(" "));
        if (openButton.getValue() && defaultAction.is(ScreenshotActions.SAVE)) baseText.append(getOpenButton().append(" "));
        if (discardButton.getValue() && !defaultAction.is(ScreenshotActions.NONE)) baseText.append(getDiscardButton().append(" "));
        ChatUtil.info(baseText);
    }


    // acciones con la captura

    public void copyScreenshot(NativeImage nativeImage) {
        try {
            BufferedImage bufferedImage = new BufferedImage(nativeImage.getWidth(), nativeImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < nativeImage.getWidth(); x++)
                for (int y = 0; y < nativeImage.getHeight(); y++)
                    bufferedImage.setRGB(x, y, nativeImage.getPixel(x, y));

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
        image.writeToFile(screenshotFile);
        ChatUtil.info(Component.literal("captura guardada como: ").append(
                Component.literal(screenshotFile.getName())
                        .withStyle(ChatFormatting.UNDERLINE)
                        .withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(screenshotFile.getAbsoluteFile())))
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
    private MutableComponent getSaveButton() {
        MutableComponent button = Component.literal("[GUARDAR]");
        MutableComponent hintBaseText = Component.literal("");

        MutableComponent hintMsg = Component.literal(ScreenshotActions.SAVE.toString());
        hintMsg.setStyle(hintBaseText.getStyle().applyFormat(ChatFormatting.GRAY));
        hintBaseText.append(hintMsg);

        button.setStyle(button.getStyle()
                .applyFormats(ChatFormatting.BOLD, ChatFormatting.GOLD)
                .withClickEvent(new ClientClickEvent(CommandManager.getCommandByName("guardarcaptura").toString()))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return button;
    }

    @SuppressWarnings("DataFlowIssue")
    private MutableComponent getCopyButton() {
        MutableComponent button = Component.literal("[COPIAR]");
        MutableComponent hintBaseText = Component.literal("");

        MutableComponent hintMsg = Component.literal(ScreenshotActions.COPY.toString());
        hintMsg.setStyle(hintBaseText.getStyle().withColor(ChatFormatting.GRAY));
        hintBaseText.append(hintMsg);

        button.setStyle(button.getStyle()
                .applyFormats(ChatFormatting.BOLD, ChatFormatting.AQUA)
                .withClickEvent(new ClientClickEvent(CommandManager.getCommandByName("copiarcaptura").toString()))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return button;
    }

    private MutableComponent getOpenButton() {
        MutableComponent button = Component.literal("[ABRIR]");
        MutableComponent hintBaseText = Component.literal("");

        MutableComponent hintMsg = Component.literal("abrir archivo de imagen");
        hintMsg.setStyle(hintBaseText.getStyle().applyFormats(ChatFormatting.GRAY));
        hintBaseText.append(hintMsg);

        button.setStyle(button.getStyle()
                .applyFormats(ChatFormatting.BOLD, ChatFormatting.GRAY)
                .withClickEvent(new ClickEvent.OpenFile(screenshotFile))
                .withHoverEvent(new HoverEvent.ShowText(hintBaseText)));

        return button;
    }

    @SuppressWarnings("DataFlowIssue")
    private MutableComponent getDiscardButton() {
        MutableComponent button = Component.literal("[DESCARTAR]");
        MutableComponent hintBaseText = Component.literal("");

        MutableComponent hintMsg = Component.literal(ScreenshotActions.NONE.toString());
        hintMsg.setStyle(hintBaseText.getStyle().applyFormats(ChatFormatting.GRAY));
        hintBaseText.append(hintMsg);

        button.setStyle(button.getStyle()
                .applyFormats(ChatFormatting.BOLD, ChatFormatting.DARK_RED)
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
