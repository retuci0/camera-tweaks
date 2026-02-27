package me.retucio.sputnik.command.commands;

import com.github.retucio.neutrino.EventListener;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.event.TickEvent;
import me.retucio.sputnik.ui.screen.PreviewScreen;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
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

    @EventListener
    public void onTick(TickEvent.Post event) {
        if (shouldOpenEchest) {
            shouldOpenEchest = false;
            if (InventoryUtil.getEchestInv() != null)
                mc.setScreen(new PreviewScreen(InventoryUtil.getEchestInv(), null));
        }
    }
}