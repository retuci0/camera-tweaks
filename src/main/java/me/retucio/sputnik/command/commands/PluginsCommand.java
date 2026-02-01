package me.retucio.sputnik.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import com.mojang.brigadier.tree.RootCommandNode;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.command.Command;
import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.network.PacketEvent;
import me.retucio.sputnik.util.ChatUtil;

import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;

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
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.isIntegratedServerRunning()) {
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
            ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
            if (networkHandler == null || networkHandler.getCommandDispatcher() == null) {
                return;
            }

            RootCommandNode<ClientCommandSource> root = networkHandler.getCommandDispatcher().getRoot();
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

    private void checkNodeForPlugins(CommandNode<ClientCommandSource> node) {
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

    @SubscribeEvent
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!scanning) return;

        if (event.getPacket() instanceof CommandTreeS2CPacket) {
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