package me.retucio.camtweaks.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.camtweaks.command.Command;
import me.retucio.camtweaks.command.args.ModuleArgumentType;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// asignar una tecla a un módulo, lógica similar a BindButton
public class BindCommand extends Command {

    private static Module listeningModule = null;

    public BindCommand() {
        super("bind", "asigna una tecla a un módulo", "keybind");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(argument("módulo", ModuleArgumentType.INSTANCE)
                        .executes(ctx -> {
                            Module module = ctx.getArgument("módulo", Module.class);
                            listeningModule = module;
                            ChatUtil.info("presiona una tecla para asignarla al módulo " + Formatting.GREEN + module.getName());
                            return SUCCESS;
                        })
                        .then(literal("reset").executes(ctx -> {
                            Module module = ctx.getArgument("módulo", Module.class);
                            module.getBind().reset();
                            ChatUtil.info("tecla para el módulo " + Formatting.GREEN + module.getName() + " restablecida a " + Formatting.AQUA + KeyUtil.getKeyName(module.getKey()));
                            return SUCCESS;
                        }))
                );
    }

    public static boolean onKeyPress(int key) {
        if (listeningModule == null) return false;
        KeySetting bind = listeningModule.getBind();
        if (bind != null)
            bind.setKey(key);

        ChatUtil.info(
                Text.of("la tecla " + Formatting.AQUA + KeyUtil.getKeyName(key) + Formatting.RESET +
                        " ha sido asignada al módulo " + Formatting.GREEN + listeningModule.getName())
        );

        listeningModule = null;
        return true;
    }
}
