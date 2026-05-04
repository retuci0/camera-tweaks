package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.command.args.ModuleArgumentType;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// asignar una tecla a un módulo, lógica similar a BindButton
public class BindCommand extends Command {

    private static Module listeningModule = null;

    public BindCommand() {
        super("bind", "asigna una tecla a un módulo", "keybind");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder
                .then(argument("módulo", ModuleArgumentType.INSTANCE)
                        .executes(ctx -> {
                            Module module = ctx.getArgument("módulo", Module.class);
                            listeningModule = module;
                            ChatUtil.info("presiona una tecla para asignarla al módulo " + ChatFormatting.GREEN + module.getName());
                            return SUCCESS;
                        })
                        .then(literal("reset").executes(ctx -> {
                            Module module = ctx.getArgument("módulo", Module.class);
                            module.getBind().reset();
                            ChatUtil.info("tecla para el módulo " + ChatFormatting.GREEN + module.getName()
                                    + " restablecida a " + ChatFormatting.AQUA + KeyUtil.getKeyName(module.getKey()));
                            return SUCCESS;
                        }))
                );
    }

    public static boolean onKeyPress(int key) {
        if (listeningModule == null) return false;
        KeySetting bind = listeningModule.getBind();

        if (ClientSettingsPanel.clientSettings.multipleKeybinds.getValue()) {
            List<KeyMapping> keys = new ArrayList<>(List.of(mc.options.keyMappings));
            keys.removeAll(List.of(mc.options.debugKeys));

            for (KeyMapping kb : keys) {
                boolean keyAlreadyBound = kb.matches(new KeyEvent(key, 0, 0));
                boolean allowMultiple = ClientSettingsPanel.clientSettings.multipleKeybinds.getValue();

                if (keyAlreadyBound && !allowMultiple) {
                    ChatUtil.warn("esa tecla ya está cogida por "
                            + ChatFormatting.GREEN + "\"" + I18n.get(kb.getName() + "\""));
                    listeningModule = null;
                    return true;
                }
            }
        }

        if (bind != null) bind.setValue(key);

        ChatUtil.info(
                Component.nullToEmpty("la tecla " + ChatFormatting.AQUA + KeyUtil.getKeyName(key) + ChatFormatting.RESET +
                        " ha sido asignada al módulo " + ChatFormatting.GREEN + listeningModule.getName())
        );

        listeningModule = null;
        return true;
    }
}
