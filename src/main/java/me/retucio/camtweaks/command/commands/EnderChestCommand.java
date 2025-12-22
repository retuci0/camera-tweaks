package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.DisconnectEvent;
import me.retucio.camtweaks.event.events.OpenScreenEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import me.retucio.camtweaks.event.events.TickEvent;
import me.retucio.camtweaks.ui.screen.PreviewScreen;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.MiscUtil;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

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
                if (MiscUtil.getEchestInv() == null) {
                    ChatUtil.warn("necesitas abrir un enderchest una vez primero");
                    return;
                }

                shouldOpenEchest = true;
            });

            return SUCCESS;
        });
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.Post event) {
        if (shouldOpenEchest) {
            shouldOpenEchest = false;
            if (MiscUtil.getEchestInv() != null)
                mc.setScreen(new PreviewScreen(MiscUtil.getEchestInv(), null));
        }
    }
}