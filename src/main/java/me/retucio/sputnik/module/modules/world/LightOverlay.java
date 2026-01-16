package me.retucio.sputnik.module.modules.world;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LightOverlay extends Module {

    SettingGroup sgDetection = addSg(new SettingGroup("detección", true));
    SettingGroup sgColors = addSg(new SettingGroup("colores", true));
    SettingGroup sgOptimization = addSg(new SettingGroup("optimización", true));

    // detección
    public NumberSetting radius = sgDetection.add(new NumberSetting(
            "radio",
            "radio a tener en cuenta al renderizar superposición",
            16, 5, 128, 1));

    public NumberSetting yRadius = sgDetection.add(new NumberSetting(
            "rango vertical",
            "distancia vertical a tener en cuenta",
            4, 1, 16, 1));

    public BooleanSetting onWater = sgDetection.add(new BooleanSetting(
            "en agua",
            "mostrar también bloques cubiertos en agua", true));

    public BooleanSetting dontCullWater = sgDetection.add(new BooleanSetting(
            "evitar culling en agua",
            "mostrar superposición a través de bloques para poder verla desde fuera del agua", false));


    // optimización, porque iba como la mierda
    public NumberSetting updateInterval = sgOptimization.add(new NumberSetting(
            "intervalo de búsqueda",
            "cada cuántos ticks actualizar la búsqueda de bloques",
            10, 1, 80, 1));

    public NumberSetting movementThreshold = sgOptimization.add(new NumberSetting(
            "umbral de movimiento",
            "distancia que debe moverse el jugador antes de forzar una actualización",
            3.0, 0.5, 10.0, 0.5));

    public BooleanSetting asyncSearch = sgOptimization.add(new BooleanSetting(
            "búsqueda asíncrona",
            "buscar bloques en un hilo separado (reduce lag)",
            true));

    public BooleanSetting incrementalUpdates = sgOptimization.add(new BooleanSetting(
            "actualizaciones incrementales",
            "actualizar solo los bloques nuevos / eliminados en lugar de recalcular todo",
            true));

    // colores
    public ColorSetting lightColor = sgColors.add(new ColorSetting(
            "color de la luz",
            "color de los bloques con luz",
            new Color(0, 255, 0, 67), false));

    public ColorSetting darknessColor = sgColors.add(new ColorSetting(
            "color de oscuridad",
            "color de bloques con 0 luz",
            new Color(255, 0, 0, 67), false));


    // caché y estado
    private final Set<BlockPos> cachedBlocks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<BlockPos> blocksToRender = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private BlockPos lastPlayerPos = null;
    private int lastRadius = -1;
    private int lastYRadius = -1;
    private int updateTimer = 0;
    private boolean isSearching = false;

    // para actualizaciones incrementales
    private int incrementalLayer = 0;
    private boolean fullRescanNeeded = false;

    public LightOverlay() {
        super("superposición de luz",
                "te muestra el nivel de luz en bloques, para prevenir mob spawns",
                Category.WORLD);

        onWater.onUpdate(v -> dontCullWater.setVisible(v));
    }

    @Override
    public void onEnable() {
        cachedBlocks.clear();
        blocksToRender.clear();
        lastPlayerPos = null;
        lastRadius = -1;
        lastYRadius = -1;
        updateTimer = 0;
        incrementalLayer = 0;
        fullRescanNeeded = true;

        super.onEnable();
    }

    @Override
    public void onDisable() {
        cachedBlocks.clear();
        blocksToRender.clear();

        super.onDisable();
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void onRenderWorld(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        // actualización periódica
        if (updateTimer++ >= updateInterval.getIntValue()) {
            updateTimer = 0;
            updateBlocks();
        }

        // renderizado
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
        int radiusValue = radius.getIntValue();
        double radiusSq = radiusValue * radiusValue;

        // actualizar lista de renderizado basado en distancia
        blocksToRender.clear();
        for (BlockPos pos : cachedBlocks) {
            double distanceSq = pos.getSquaredDistance(cameraPos);
            if (distanceSq <= radiusSq) {
                blocksToRender.add(pos);
            }
        }

        // renderizar bloques visibles
        for (BlockPos block : blocksToRender) {
            if (!mc.world.isChunkLoaded(block)) continue;

            BlockState state = mc.world.getBlockState(block);
            BlockState aboveState = mc.world.getBlockState(block.up());

            if (!isValidSpawnSurface(block, state, aboveState)) continue;

            // nivel de luz
            int light = mc.world.getLightLevel(LightType.BLOCK, block.up());

            // color
            Color color;
            if (light == 0) {
                color = darknessColor.getValue();
            } else {
                float ratio = light / 15.0f;
                color = new Color(
                        (int)(lightColor.getR() * ratio),
                        (int)(lightColor.getG() * ratio),
                        (int)(lightColor.getB() * ratio),
                        lightColor.getA()
                );
            }

            // renderizar
            boolean shouldCull = !(onWater.getValue() && aboveState.isOf(Blocks.WATER) && dontCullWater.getValue());
            RenderUtil.drawBlockFaceFilled(event.getMatrices(), block, Direction.UP, color, 0.001f, shouldCull);
        }
    }

    private void updateBlocks() {
        if (mc.player == null || mc.world == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int currentRadius = radius.getIntValue();
        int currentYRadius = yRadius.getIntValue();

        // verificar si hace falta actualizar
        boolean needsUpdate = fullRescanNeeded ||
                lastRadius != currentRadius ||
                lastYRadius != currentYRadius ||
                (lastPlayerPos != null &&
                        playerPos.getSquaredDistance(lastPlayerPos) > (movementThreshold.getValue() * movementThreshold.getValue()));

        if (!needsUpdate && incrementalUpdates.getValue() && incrementalLayer >= 0) {
            // actualización incremental de una sola capa
            performIncrementalUpdate(playerPos, currentRadius, currentYRadius);
            return;
        }

        if (!needsUpdate) {
            return;
        }

        lastPlayerPos = playerPos;
        lastRadius = currentRadius;
        lastYRadius = currentYRadius;
        fullRescanNeeded = false;
        incrementalLayer = 0;

        // búsqueda
        if (asyncSearch.getValue()) {
            if (isSearching) return;

            isSearching = true;
            new Thread(() -> {
                try {
                    performFullBlockSearch(playerPos, currentRadius, currentYRadius);
                } finally {
                    isSearching = false;
                }
            }, "sputnik-lightoverlay-search").start();
        } else {
            performFullBlockSearch(playerPos, currentRadius, currentYRadius);
        }
    }

    @SuppressWarnings("deprecation")
    private void performFullBlockSearch(BlockPos center, int radius, int yRadius) {
        Set<BlockPos> newBlocks = new HashSet<>();
        int radiusSq = radius * radius;

        int minY = Math.max(mc.world.getBottomY(), center.getY() - yRadius);
        int maxY = Math.min(mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mc.player.getBlockPos()) - 1, center.getY() + yRadius);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z > radiusSq) continue;

                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = center.add(x, y - center.getY(), z);

                    // nuh uh si el chunk no está cargado
                    if (!mc.world.isChunkLoaded(pos)) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    BlockState aboveState = mc.world.getBlockState(pos.up());

                    if (isValidSpawnSurface(pos, state, aboveState)) {
                        newBlocks.add(pos.toImmutable());
                    }
                }
            }
        }

        // actualizar caché
        cachedBlocks.clear();
        cachedBlocks.addAll(newBlocks);
    }

    @SuppressWarnings("deprecation")
    private void performIncrementalUpdate(BlockPos center, int radius, int yRadius) {
        if (incrementalLayer > radius * 2) {
            incrementalLayer = -radius;
        }

        int currentX = center.getX() - radius + incrementalLayer;
        int radiusSq = radius * radius;
        int minY = Math.max(mc.world.getBottomY(), center.getY() - yRadius);
        int maxY = Math.min(mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mc.player.getBlockPos()) - 1, center.getY() + yRadius);

        // buscar en una columna específica (x fijo, variar z)
        for (int z = -radius; z <= radius; z++) {
            int xDist = incrementalLayer;
            if (xDist*xDist + z*z > radiusSq) continue;

            for (int y = minY; y <= maxY; y++) {
                BlockPos pos = new BlockPos(currentX, y, center.getZ() + z);

                if (!mc.world.isChunkLoaded(pos)) continue;

                BlockState state = mc.world.getBlockState(pos);
                BlockState aboveState = mc.world.getBlockState(pos.up());

                boolean isValid = isValidSpawnSurface(pos, state, aboveState);

                if (isValid) {
                    cachedBlocks.add(pos.toImmutable());
                } else {
                    cachedBlocks.remove(pos);
                }
            }
        }

        incrementalLayer++;

        // cada 10 actualizaciones incrementales, hacer una limpieza de bloques lejanos
        if (incrementalLayer % 10 == 0) {
            cleanupDistantBlocks(center, radius);
        }
    }

    private void cleanupDistantBlocks(BlockPos center, int radius) {
        int radiusSq = radius * radius * 2;
        Iterator<BlockPos> iterator = cachedBlocks.iterator();

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            int dx = pos.getX() - center.getX();
            int dz = pos.getZ() - center.getZ();

            if (dx * dx + dz * dz > radiusSq) {
                iterator.remove();
            }
        }
    }

    private boolean isValidSpawnSurface(BlockPos pos, BlockState state, BlockState aboveState) {
        if (state.isAir() || !state.isOpaque() || !state.isFullCube(mc.world, pos)) {
            return false;
        }

        Block aboveBlock = aboveState.getBlock();

        if (aboveState.isAir()) return true;
        if (aboveBlock == Blocks.WATER && onWater.getValue()) return true;
        if (aboveBlock instanceof PlantBlock) return true;

        if (aboveBlock instanceof SlabBlock ||
                aboveBlock instanceof StairsBlock ||
                aboveBlock instanceof ButtonBlock ||
                aboveBlock instanceof CarpetBlock ||
                aboveBlock instanceof PressurePlateBlock ||
                aboveBlock instanceof PaneBlock ||
                aboveBlock instanceof TrapdoorBlock ||
                aboveBlock instanceof FenceBlock ||
                aboveBlock instanceof WallBlock ||
                aboveBlock instanceof AnvilBlock ||
                aboveBlock instanceof BedBlock) {
            return false;
        }

        if (aboveBlock == Blocks.CHEST ||
                aboveBlock == Blocks.ENDER_CHEST ||
                aboveBlock == Blocks.BARREL ||
                aboveBlock == Blocks.TRAPPED_CHEST ||
                aboveBlock == Blocks.ENCHANTING_TABLE ||
                aboveBlock == Blocks.GRINDSTONE ||
                aboveBlock == Blocks.STONECUTTER ||
                aboveBlock == Blocks.LOOM ||
                aboveBlock == Blocks.COMPOSTER ||
                aboveBlock == Blocks.CAKE) {
            return false;
        }

        return !aboveState.isOpaque() || !aboveState.isFullCube(mc.world, pos.up());
    }
}