package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.command.args.EnumArgumentType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.slot.SlotActionType;

// https://cdn.discordapp.com/attachments/1456079799045980400/1457154232737206375/dupersunited-public-addon-1.21.11-3.1.3.jar?ex=69735be1&is=69720a61&hm=77fb6faee49762e0a812661f0d98931e89cb725db711c45722249d0efe3b91e4&
public class ClickSlotCommand extends Command {

    public ClickSlotCommand() {
        super("clickslot", "simula un clic a un slot");
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("slot", IntegerArgumentType.integer())
                .then(argument("botón", IntegerArgumentType.integer())
                        .then(argument("acción", EnumArgumentType.enumArgument(SlotActionType.SWAP))
                                .executes(context -> {
                                    int slot = context.getArgument("slot", Integer.class), button = context.getArgument("botón", Integer.class);
                                    SlotActionType action = context.getArgument("acción", SlotActionType.class);
                                    Screen screen = mc.currentScreen;
                                    int syncId = (screen instanceof GenericContainerScreen gcScreen) ? gcScreen.getScreenHandler().syncId : 0;
                                    mc.interactionManager.clickSlot(syncId, slot, button, action, mc.player);
                                    return SUCCESS;
                                })
                        )
                )
        );
    }
}
