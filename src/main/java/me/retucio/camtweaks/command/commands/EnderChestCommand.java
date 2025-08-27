package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;

// accede a los contenidos de tu enderchest
public class EnderChestCommand extends Command {

    public EnderChestCommand() {
        super("echest", "muestra el contenido de tu enderchest", "ec", "enderchest");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            mc.execute(() -> {  // para evitar llamar setScreen desde el hilo equivocado, que causa crashes
                EnderChestInventory enderChestInv = mc.player.getEnderChestInventory();

                // llámame imbécil pero no tengo ni idea de por qué no se abre
                mc.setScreen(new GenericContainerScreen(
                        GenericContainerScreenHandler.createGeneric9x3(mc.player.currentScreenHandler.syncId + 1, mc.player.getInventory(), enderChestInv),
                        mc.player.getInventory(),
                        Text.translatable("container.enderchest")
                ));
            });
            return SUCCESS;
        });
    }
}