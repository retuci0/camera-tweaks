package me.retucio.sputnik.ui.screen;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.util.misc.VersionChecker;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;


public class UpdateScreen extends Screen {

    private final Minecraft mc = Minecraft.getInstance();
    private final LinearLayout grid = LinearLayout.vertical();

    private final URI updateLink = URI.create(FabricLoader.getInstance()
            .getModContainer(Sputnik.MOD_ID)
            .orElseThrow()
            .getMetadata()
            .getContact()
            .get("sources").
            get()
            + "/releases/latest"
    );

    private final Component outdatedText = Component.literal("version desactualizada: " + Sputnik.MOD_VERSION);
    private final Component updateText = Component.literal("descarga la última versión (" + VersionChecker.getLatestVersion() + ") desde aquí:");

    private final Button idgafButton = Button.builder(
            Component.literal("me la pela"),
            button -> this.onClose()
    ).build();

    private final Button updateButton = Button.builder(
            Component.literal("CLIC AQUÍ"),
    button -> clickUrlAction(mc, this, updateLink)
    ).build();


    public UpdateScreen() {
        super(Component.literal(ChatFormatting.BOLD + "actualizar"));
    }

    @Override
    protected void init() {
        super.init();
        this.grid.defaultCellSetting().alignHorizontallyCenter().padding(10);

        this.grid.addChild(new StringWidget(this.title, this.font));
        this.grid.addChild(new StringWidget(outdatedText, this.font));
        this.grid.addChild(new StringWidget(updateText, this.font));

        this.grid.defaultCellSetting().padding(2);

        this.grid.addChild(updateButton);
        this.grid.addChild(idgafButton);

        this.grid.arrangeElements();
        this.grid.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        FrameLayout.centerInRectangle(this.grid, this.getRectangle());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean canInterruptWithAnotherScreen() {
        return true;
    }
}
