package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.command.args.EntityArgumentType;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.KeyEvent;
import me.retucio.camtweaks.util.MiscUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

public class SpectateCommand extends Command {

    public SpectateCommand() {
        super("espectar", "te permite adquirir el punto de vista de una entidad", "spectate", "spec");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .executes(ctx -> {  // If no argument, use the entity player is looking at
                    mc.execute(() -> {
                        Entity target = MiscUtil.getEntityPlayerIsLookingAt();
                        if (target != null)
                            mc.setCameraEntity(target);
                    });
                    return SUCCESS;
                })
                .then(argument("entidad", EntityArgumentType.INSTANCE)
                        .executes(ctx -> {
                            Entity entity = EntityArgumentType.get(ctx, "entidad");
                            if (mc.player != null && entity != null) {
                                mc.setCameraEntity(entity);
                            }
                            return SUCCESS;
                        })
                );
    }

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        if (mc.getCameraEntity() != mc.player && mc.currentScreen == null
                && event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() != GLFW.GLFW_RELEASE ) {
            event.cancel();
            mc.setCameraEntity(mc.player);
        }
    }
}