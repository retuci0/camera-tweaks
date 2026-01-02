package me.retucio.camtweaks.module.modules.network;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.AddEntityEvent;
import me.retucio.camtweaks.event.events.RenderWorldEvent;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.misc.FakePlayer;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.MiscUtil;
import me.retucio.camtweaks.util.render.RenderUtil;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.dimension.DimensionType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


// lógica sutilmente robada de MeteorClient porque la mía era una mierda
public class LogoutSpots extends Module {

    public BooleanSetting outlines = addSetting(new BooleanSetting("contorno", "renderizar contorno de la caja (lados)", true));
    public ColorSetting outlineColor = addSetting(new ColorSetting("color del contorno", "Color de las líneas", new Color(255, 0, 0, 230), false));
    public NumberSetting lineWidth = addSetting(new NumberSetting("grosor de línea", "grosor de las líneas del contorno", 1.5, 0.5, 5, 0.1));

    public BooleanSetting filling = addSetting(new BooleanSetting("relleno", "renderizar relleno de la caja (caras)", true));
    public ColorSetting fillingColor = addSetting(new ColorSetting("color del relleno", "Color de los lados", new Color(230, 0, 0, 55), false));

    public BooleanSetting fullHeight = addSetting(new BooleanSetting("hitbox completa", "renderizar la caja completa de la hitbox, o solo marcar la posición", true));

    public BooleanSetting dummy = addSetting(new BooleanSetting("monigote", "spawnear un monigote para marcar la posición", true));

    private final List<LogoutEntry> players = new ArrayList<>();
    private final List<PlayerListEntry> lastPlayerList = new ArrayList<>();
    private final List<PlayerEntity> lastPlayers = new ArrayList<>();

    private int timer;
    private DimensionType lastDimension;

    public LogoutSpots() {
        super("puntos de desconexión",
                "te muestra los puntos donde otros jugadores se desconectan",
                Category.NETWORK);

        outlines.onUpdate(v -> {
            outlineColor.setVisible(v);
            fullHeight.setVisible(v || filling.isEnabled());
        });

        filling.onUpdate(v -> {
            fillingColor.setVisible(v);
            fullHeight.setVisible(v || outlines.isEnabled());
        });

        dummy.onUpdate(v -> {
            if (!v) removeDummies();
        });
    }

    @Override
    public void onEnable() {
        if (mc.getNetworkHandler() != null) {
            lastPlayerList.clear();
            lastPlayerList.addAll(mc.getNetworkHandler().getPlayerList());
        }

        updateLastPlayers();

        timer = 10;
        if (mc.world != null) lastDimension = mc.world.getDimension();

        super.onEnable();
    }

    @Override
    public void onDisable() {
        removeDummies();

        players.clear();
        lastPlayerList.clear();
        lastPlayers.clear();

        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.getNetworkHandler() == null) return;

        if (mc.getNetworkHandler().getPlayerList().size() != lastPlayerList.size()) {
            for (PlayerListEntry entry : lastPlayerList) {
                boolean stillOnline = mc.getNetworkHandler().getPlayerList().stream()
                        .anyMatch(playerEntry -> playerEntry.getProfile().id().equals(entry.getProfile().id()));

                if (stillOnline) continue;

                // encontrar al jugador que se ha desconectado
                for (PlayerEntity player : lastPlayers) {
                    if (player.getUuid().equals(entry.getProfile().id())) {
                        LogoutEntry logoutEntry = new LogoutEntry(player);

                        if (mc.world != null && !mc.world.hasEntity(logoutEntry.dummy) && dummy.isEnabled())
                            mc.world.addEntity(logoutEntry.dummy);

                        addLogoutSpot(logoutEntry);
                        break;
                    }
                }
            }

            lastPlayerList.clear();
            lastPlayerList.addAll(mc.getNetworkHandler().getPlayerList());
            updateLastPlayers();
        }

        if (timer <= 0) {
            updateLastPlayers();
            timer = 10;
        } else {
            timer--;
        }

        // borrar todos los logout spots al cambiar de dimensión
        DimensionType currentDimension = mc.world.getDimension();
        if (currentDimension != lastDimension) {
            for (LogoutEntry entry : players) {
                if (entry.dummy != null) {
                    entry.dummy.remove(Entity.RemovalReason.KILLED);
                    entry.dummy.onRemoved();
                }
            }
            players.clear();
        }
        lastDimension = currentDimension;
    }

    @SubscribeEvent
    public void onEntityAdded(AddEntityEvent event) {
        if (event.getEntity() instanceof PlayerEntity player) {
            for (LogoutEntry entry : players) {
                if (entry.uuid.equals(player.getUuid())) {
                    if (entry.dummy != null && mc.world != null) {
                        if (mc.world.getEntityById(entry.dummy.getId()) != null) {
                            entry.dummy.remove(Entity.RemovalReason.KILLED);
                            entry.dummy.onRemoved();
                        }
                    }
                    players.remove(entry);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldEvent event) {
        if (mc.player == null || mc.world == null) return;

        for (LogoutEntry entry : players)
            entry.renderBox(event.getMatrices());
    }

    private void addLogoutSpot(LogoutEntry entry) {
        for (LogoutEntry e : players) {
            if (e.uuid.equals(entry.uuid)) {
                if (e.dummy != null && mc.world != null) {
                    if (mc.world.getEntityById(e.dummy.getId()) != null) {
                        e.dummy.remove(Entity.RemovalReason.KILLED);
                        e.dummy.onRemoved();
                    }
                }
                players.remove(e);
                break;
            }
        }

        players.add(entry);
    }

    private void updateLastPlayers() {
        if (mc.world == null) return;

        lastPlayers.clear();
        for (Entity entity : mc.world.getEntities())
            if (entity instanceof PlayerEntity player && player != mc.player)
                lastPlayers.add(player);
    }

    private void removeDummies() {
        for (LogoutEntry entry : players) {
            if (entry.dummy != null) {
                entry.dummy.remove(Entity.RemovalReason.KILLED);
                entry.dummy.onRemoved();
            }
        }
    }


    private class LogoutEntry {

        public final double x, y, z;
        public final double xWidth, zWidth, halfWidth, height;

        public final UUID uuid;
        public final String name;
        public final String logoutTime;

        public final String dummyName;
        public final OtherClientPlayerEntity dummy;

        public LogoutEntry(PlayerEntity player) {
            this.halfWidth = player.getWidth() / 2;
            this.x = player.getX() - halfWidth;
            this.y = player.getY();
            this.z = player.getZ() - halfWidth;

            Box box = player.getBoundingBox();
            this.xWidth = box.getLengthX();
            this.zWidth = box.getLengthZ();
            this.height = box.getLengthY();

            this.uuid = player.getUuid();
            this.name = player.getName().getString();
            this.logoutTime = MiscUtil.getCurrentFormattedTime();

            this.dummyName = name + " (" + logoutTime + ")";
            this.dummy = ModuleManager.INSTANCE.getModuleByClass(FakePlayer.class).addPlayer(player, dummyName);

            this.dummy.updatePositionAndAngles(x + halfWidth, y, z + halfWidth,
                    player.getYaw(), player.getPitch());
        }

        public void renderBox(MatrixStack matrices) {
            if (fullHeight.isEnabled()) {
                if (outlines.isEnabled())
                    RenderUtil.drawOutlineBox(matrices, new Box(x, y, z, x + xWidth, y + height, z + zWidth), outlineColor.getColor(), lineWidth.getFloatValue());
                if (filling.isEnabled())
                    RenderUtil.drawFilledBox(matrices, new Box(x, y, z, x + xWidth, y + height, z + zWidth), fillingColor.getColor());
            } else {
                if (outlines.isEnabled()) {
                    Box bottomBox = new Box(x, y, z, x + xWidth, y + 0.01, z + zWidth);
                    RenderUtil.drawOutlineBox(matrices, bottomBox, outlineColor.getColor(), lineWidth.getFloatValue());
                }
                if (filling.isEnabled()) {
                    RenderUtil.drawBlockFaceFilled(matrices,
                            net.minecraft.util.math.BlockPos.ofFloored(x + halfWidth, y, z + halfWidth),
                            net.minecraft.util.math.Direction.UP, fillingColor.getColor(), 0.001f);
                }
            }
        }
    }
}
