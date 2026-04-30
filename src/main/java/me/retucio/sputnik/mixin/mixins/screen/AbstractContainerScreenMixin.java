package me.retucio.sputnik.mixin.mixins.screen;

import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.inventory.Burglar;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private void addStealAndDumpButtons(CallbackInfo ci) {
        Burglar burglar = ModuleManager.INSTANCE.getModuleByClass(Burglar.class);
        if (!burglar.isEnabled()) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        int bw = 50;
        int bh = 20;
        int gap = 10;
        int w = bw * 2 + gap;

        int bgLeft = (screen.width - screen.imageWidth) / 2;
        int bgTop = (screen.height - screen.imageHeight) / 2;

        int bx = bgLeft + (screen.imageWidth - w) / 2;
        int by = bgTop - bh - 5;

        if (burglar.showStealButton.getValue()) {
            Button stealButton = Button.builder(Component.literal("robar"), bnutton -> {
                burglar.steal(screen);
            }).bounds(bx, by, bw, bh).build();
            screen.addRenderableWidget(stealButton);
        }

        if (burglar.showDumpButton.getValue()) {
            Button dumpButton = Button.builder(Component.literal("dejar"), button -> {
                burglar.dump(screen);
            }).bounds(bx + bw + gap, by, bw, bh).build();
            screen.addRenderableWidget(dumpButton);
        }
    }
}