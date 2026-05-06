package me.retucio.sputnik.module.modules.network;

import com.github.retucio.neutrino.EventListener;
import com.mojang.blaze3d.vertex.PoseStack;
import me.retucio.sputnik.event.network.AddEntityEvent;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.FakePlayer;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.MiscUtil;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


// lógica sutilmente robada de MeteorClient porque la mía era una mierda
public class LogoutSpots extends Module {

    private final SettingGroup sgOutlines = addSg(new SettingGroup("contorno", true));
    private final SettingGroup sgFilling = addSg(new SettingGroup("relleno", true));
    private final SettingGroup sgMisc = addSg(new SettingGroup("misc.", true));

    private final BooleanSetting outlines = sgOutlines.add(new BooleanSetting("contorno", "renderizar contorno de la caja (lados)", true));
    private final ColorSetting outlineColor = sgOutlines.add(new ColorSetting("color del contorno", "Color de las líneas", new Color(255, 0, 0, 230), false));
    private final NumberSetting lineWidth = sgOutlines.add(new NumberSetting("grosor de línea", "grosor de las líneas del contorno", 1.5, 0.5, 5, 0.1));

    private final BooleanSetting filling = sgFilling.add(new BooleanSetting("relleno", "renderizar relleno de la caja (caras)", true));
    private final ColorSetting fillingColor = sgFilling.add(new ColorSetting("color del relleno", "Color de los lados", new Color(230, 0, 0, 55), false));

    private final BooleanSetting fullHeight = sgMisc.add(new BooleanSetting("hitbox completa", "renderizar la caja completa de la hitbox, o solo marcar la posición", true));

    private final BooleanSetting dummy = sgMisc.add(new BooleanSetting("monigote", "spawnear un monigote para marcar la posición", true));

    private final List<LogoutEntry> players = new ArrayList<>();
    private final List<PlayerInfo> lastPlayerList = new ArrayList<>();
    private final List<Player> lastPlayers = new ArrayList<>();

    private int timer;
    private DimensionType lastDimension;

    public LogoutSpots() {
        super("puntos de desconexión",
                "te muestra los puntos donde otros jugadores se desconectan",
                Category.NETWORK);

        outlines.onUpdate(v -> {
            outlineColor.visibility(v);
            fullHeight.visibility(v || filling.getValue());
        });

        filling.onUpdate(v -> {
            fillingColor.visibility(v);
            fullHeight.visibility(v || outlines.getValue());
        });

        dummy.onUpdate(v -> {
            if (!v) removeDummies();
        });
    }

    @Override
    public void onEnable() {
        if (mc.getConnection() != null) {
            lastPlayerList.clear();
            lastPlayerList.addAll(mc.getConnection().getOnlinePlayers());
        }

        updateLastPlayers();

        timer = 10;
        if (mc.level != null) lastDimension = mc.level.dimensionType();

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
        if (mc.level == null || mc.getConnection() == null) return;

        if (mc.getConnection().getOnlinePlayers().size() != lastPlayerList.size()) {
            for (PlayerInfo entry : lastPlayerList) {
                boolean stillOnline = mc.getConnection().getOnlinePlayers().stream()
                        .anyMatch(playerEntry -> playerEntry.getProfile().id().equals(entry.getProfile().id()));

                if (stillOnline) continue;

                // encontrar al jugador que se ha desconectado
                for (Player player : lastPlayers) {
                    if (player.getUUID().equals(entry.getProfile().id())) {
                        LogoutEntry logoutEntry = new LogoutEntry(player);

                        if (mc.level != null && !mc.level.isTickingEntity(logoutEntry.dummy) && dummy.getValue())
                            mc.level.addEntity(logoutEntry.dummy);

                        addLogoutSpot(logoutEntry);
                        break;
                    }
                }
            }

            lastPlayerList.clear();
            lastPlayerList.addAll(mc.getConnection().getOnlinePlayers());
            updateLastPlayers();
        }

        if (timer <= 0) {
            updateLastPlayers();
            timer = 10;
        } else {
            timer--;
        }

        // borrar todos los logout spots al cambiar de dimensión
        DimensionType currentDimension = mc.level.dimensionType();
        if (currentDimension != lastDimension) {
            for (LogoutEntry entry : players) {
                if (entry.dummy != null) {
                    entry.dummy.remove(Entity.RemovalReason.KILLED);
                    entry.dummy.onClientRemoval();
                }
            }
            players.clear();
        }
        lastDimension = currentDimension;
    }

    @EventListener
    private void onEntityAdded(AddEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            for (LogoutEntry entry : players) {
                if (entry.uuid.equals(player.getUUID())) {
                    if (entry.dummy != null && mc.level != null) {
                        if (mc.level.getEntity(entry.dummy.getId()) != null) {
                            entry.dummy.remove(Entity.RemovalReason.KILLED);
                            entry.dummy.onClientRemoval();
                        }
                    }
                    players.remove(entry);
                    break;
                }
            }
        }
    }

    @EventListener
    private void onWorldRender(Render3DEvent event) {
        if (mc.player == null || mc.level == null) return;

        for (LogoutEntry entry : players)
            entry.renderBox(event.getMatrices());
    }

    private void addLogoutSpot(LogoutEntry entry) {
        for (LogoutEntry e : players) {
            if (e.uuid.equals(entry.uuid)) {
                if (e.dummy != null && mc.level != null) {
                    if (mc.level.getEntity(e.dummy.getId()) != null) {
                        e.dummy.remove(Entity.RemovalReason.KILLED);
                        e.dummy.onClientRemoval();
                    }
                }
                players.remove(e);
                break;
            }
        }

        players.add(entry);
    }

    private void updateLastPlayers() {
        if (mc.level == null) return;

        lastPlayers.clear();
        for (Entity entity : mc.level.getEntities().getAll())
            if (entity instanceof Player player && player != mc.player)
                lastPlayers.add(player);
    }

    private void removeDummies() {
        for (LogoutEntry entry : players) {
            if (entry.dummy != null) {
                entry.dummy.remove(Entity.RemovalReason.KILLED);
                entry.dummy.onClientRemoval();
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
        public final RemotePlayer dummy;

        public LogoutEntry(Player player) {
            this.halfWidth = player.getBbWidth() / 2;
            this.x = player.getX() - halfWidth;
            this.y = player.getY();
            this.z = player.getZ() - halfWidth;

            AABB box = player.getBoundingBox();
            this.xWidth = box.getXsize();
            this.height = box.getYsize();
            this.zWidth = box.getZsize();

            this.uuid = player.getUUID();
            this.name = player.getName().getString();
            this.logoutTime = MiscUtil.getCurrentFormattedTime();

            this.dummyName = name + " (" + logoutTime + ")";
            this.dummy = ModuleManager.INSTANCE.getModuleByClass(FakePlayer.class).addPlayer(player, dummyName);

            this.dummy.absSnapTo(x + halfWidth, y, z + halfWidth,
                    player.getYRot(), player.getXRot());
        }

        public void renderBox(PoseStack matrices) {
            if (fullHeight.getValue()) {
                if (outlines.getValue())
                    RenderUtil.drawOutlineBox(matrices, new AABB(x, y, z, x + xWidth, y + height, z + zWidth), outlineColor.getValue(), lineWidth.getFloatValue(), true);
                if (filling.getValue())
                    RenderUtil.drawFilledBox(matrices, new AABB(x, y, z, x + xWidth, y + height, z + zWidth), fillingColor.getValue(), true);
            } else {
                if (outlines.getValue()) {
                    RenderUtil.drawBlockFaceOutlines(matrices,
                            BlockPos.containing(x + halfWidth, y, z + halfWidth),
                            Direction.UP, outlineColor.getValue(), lineWidth.getFloatValue(), true);
                }
                if (filling.getValue()) {
                    RenderUtil.drawBlockFaceFilled(matrices,
                            BlockPos.containing(x + halfWidth, y, z + halfWidth),
                            Direction.UP, fillingColor.getValue(), 0.001f, true);
                }
            }
        }
    }
}
