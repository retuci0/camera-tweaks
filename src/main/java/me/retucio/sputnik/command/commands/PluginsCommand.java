package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import com.mojang.brigadier.tree.RootCommandNode;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.util.ChatUtil;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;

import java.util.HashSet;
import java.util.Set;


public class PluginsCommand extends Command {
    private final Set<String> plugins = new HashSet<>();
    private boolean scanning = false;

    public PluginsCommand() {
        super("plugins", "busca los plugins que tenga el server", "pl", "serverplugins");
        Sputnik.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.executes(context -> {
            if (mc.hasSingleplayerServer()) {
                ChatUtil.error("estás en un solo jugador");
                return SUCCESS;
            }

            if (scanning) {
                ChatUtil.error("calmaaa");
                return SUCCESS;
            }

            plugins.clear();
            scanning = true;

            findNamespaces();

            return SUCCESS;
        });
    }

    private void findNamespaces() {
        try {
            ClientPacketListener networkHandler = mc.getConnection();
            if (networkHandler == null) {
                return;
            }

            RootCommandNode<ClientSuggestionProvider> root = networkHandler.getCommands().getRoot();
            root.getChildren().forEach(this::checkNodeForPlugins);

            if (!plugins.isEmpty()) {
                ChatUtil.info(plugins.size() + " namespace(s) encontrado(s) de comandos:");
                plugins.stream()
                        .sorted()
                        .forEach(plugin -> ChatUtil.info("  - " + plugin));
            }

            displayResults();

        } catch (Exception e) {
            ChatUtil.error("error analizando comandos: " + e.getMessage());
        }
    }

    private void checkNodeForPlugins(CommandNode<ClientSuggestionProvider> node) {
        String name = node.getName();

        if (name.contains(":")) {
            String namespace = name.split(":")[0];
            if (!namespace.equalsIgnoreCase("minecraft")
                    && !namespace.equalsIgnoreCase("bukkit")
                    && !namespace.equalsIgnoreCase("spigot")
                    && !namespace.equalsIgnoreCase("paper")) {
                plugins.add(namespace);
            }
        }

        node.getChildren().forEach(this::checkNodeForPlugins);
    }

    private void onPacketReceive(PacketEvent.Receive event) {
        if (!scanning) return;

        if (event.getPacket() instanceof ClientboundCommandsPacket) {
            findNamespaces();
        }
    }

    private void displayResults() {
        if (!plugins.isEmpty()) {
                ChatUtil.info(plugins.size() + " plugin(s) encontrado(s)");
                plugins.stream().sorted()
                        .forEach(plugin -> ChatUtil.info("  - " + plugin));
        } else {
            ChatUtil.warn("sin plugins (probablemente)");
        }

        scanning = false;
    }
}