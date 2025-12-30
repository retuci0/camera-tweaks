package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import me.retucio.camtweaks.ui.screen.PreviewScreen;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.InventoryUtil;
import net.minecraft.command.CommandSource;

// accede a los contenidos de tu enderchest
public class EnderChestCommand extends Command {

    private boolean shouldOpenEchest = false;

    public EnderChestCommand() {
        super("echest", "muestra el contenido de tu enderchest (solo de ver)", "ec", "enderchest");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            mc.execute(() -> {
                if (mc.player == null) return;
                if (InventoryUtil.getEchestInv() == null) {
                    ChatUtil.warn("necesitas abrir un enderchest una vez primero");
                    return;
                }

                shouldOpenEchest = true;
            });

            return SUCCESS;
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.Post event) {
        if (shouldOpenEchest) {
            shouldOpenEchest = false;
            if (InventoryUtil.getEchestInv() != null)
                mc.setScreen(new PreviewScreen(InventoryUtil.getEchestInv(), null));
        }
    }
}