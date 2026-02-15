package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.command.args.EntityArgumentType;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.util.EntityUtil;
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
                        Entity target = EntityUtil.getEntityPlayerIsLookingAt();
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

    public void onKey(KeyEvent event) {
        if (mc.getCameraEntity() != mc.player && mc.currentScreen == null
                && event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() != GLFW.GLFW_RELEASE ) {
            event.cancel();
            mc.setCameraEntity(mc.player);
        }
    }
}