package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.command.args.PlayerArgumentType;
import me.retucio.sputnik.util.MiscUtil;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.commands.SharedSuggestionProvider;

// comando que debería de mostrar el inventario de un jugador, pero por cómo funcionan los servers, solo te deja ver el ítem que tenga en la mano
public class PeekCommand extends Command {

    public PeekCommand() {
        super("inventario", "cotillea el inventario de otro jugador", "invsee", "peek", "inv");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder
                .then(argument("jugador", PlayerArgumentType.INSTANCE)
                        .executes(ctx -> {
                            MiscUtil.screen = new InventoryScreen(PlayerArgumentType.get(ctx));
                            return SUCCESS;
                }));
    }
}