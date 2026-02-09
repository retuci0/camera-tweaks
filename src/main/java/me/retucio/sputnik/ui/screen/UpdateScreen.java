package me.retucio.sputnik.ui.screen;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.util.VersionChecker;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UpdateScreen extends Screen {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final DirectionalLayoutWidget grid = DirectionalLayoutWidget.vertical();

    private final Text outdatedText = Text.of("version desactualizada: " + Sputnik.MOD_VERSION);
    private final Text updateText = Text.of("descarga la última versión (" + VersionChecker.getLatestVersion() + ") desde aquí:");
    private final URI updateLink = URI.create(FabricLoader.getInstance()
            .getModContainer(Sputnik.MOD_ID)
            .orElseThrow()
            .getMetadata()
            .getContact()
            .get("sources").
            get()
            + "/releases/latest"
    );

    public UpdateScreen() {
        super(Text.literal(Formatting.BOLD + "actualizar"));
    }

    @Override
    protected void init() {
        super.init();
        this.grid.getMainPositioner().alignHorizontalCenter().margin(10);

        this.grid.add(new TextWidget(this.title, this.textRenderer));
        this.grid.add(new TextWidget(outdatedText, this.textRenderer));
        this.grid.add(new TextWidget(updateText, this.textRenderer));

        this.grid.getMainPositioner().margin(2);

        ButtonWidget updateButton = ButtonWidget.builder(
                Text.of("CLIC AQUÍ"),
                button -> {
                    handleOpenUri(mc, this, updateLink);
                }
        ).build();
        this.grid.add(updateButton);

        ButtonWidget idgafButton = ButtonWidget.builder(
                Text.of("me la pela"),
                button -> this.close()
        ).build();
        this.grid.add(idgafButton);

        this.grid.refreshPositions();
        this.grid.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        SimplePositioningWidget.setPos(this.grid, this.getNavigationFocus());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
