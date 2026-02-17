package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.command.args.ModuleArgumentType;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

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

        if (ClientSettingsFrame.guiSettings.multipleKeybinds.getValue()) {
            List<KeyBinding> keys = new ArrayList<>(List.of(mc.options.allKeys));
            keys.removeAll(List.of(mc.options.debugKeys));

            for (KeyBinding kb : keys) {
                boolean keyAlreadyBound = kb.matchesKey(new KeyInput(key, 0, 0));
                boolean allowMultiple = ClientSettingsFrame.guiSettings.multipleKeybinds.getValue();

                if (keyAlreadyBound && !allowMultiple) {
                    ChatUtil.warn("esa tecla ya está cogida por "
                            + Formatting.GREEN + "\"" + I18n.translate(kb.getId()) + "\"");
                    listeningModule = null;
                    return true;
                }
            }
        }

        if (bind != null) bind.setValue(key);

        ChatUtil.info(
                Text.of("la tecla " + Formatting.AQUA + KeyUtil.getKeyName(key) + Formatting.RESET +
                        " ha sido asignada al módulo " + Formatting.GREEN + listeningModule.getName())
        );

        listeningModule = null;
        return true;
    }
}
