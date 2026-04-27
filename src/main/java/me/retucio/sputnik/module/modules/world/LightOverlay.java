package me.retucio.sputnik.module.modules.world;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.MiscUtil;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class LightOverlay extends Module {

    private final SettingGroup sgDetection = addSg(new SettingGroup("detección", true));
    private final SettingGroup sgColors = addSg(new SettingGroup("colores", true));
    private final SettingGroup sgOptimization = addSg(new SettingGroup("optimización", true));


    // detección

    private final NumberSetting radius = sgDetection.add(new NumberSetting(
            "radio",
            "radio a tener en cuenta al renderizar superposición",
            16, 5, 128, 1));

    private final NumberSetting yRadius = sgDetection.add(new NumberSetting(
            "rango vertical",
            "distancia vertical a tener en cuenta",
            4, 1, 16, 1));

    private final BooleanSetting onWater = sgDetection.add(new BooleanSetting(
            "en agua",
            "mostrar también bloques cubiertos en agua", true));

    private final BooleanSetting dontCullWater = sgDetection.add(new BooleanSetting(
            "evitar culling en agua",
            "mostrar superposición a través de bloques para poder verla desde fuera del agua", false));



    // optimización, porque iba como la mierda

    private final NumberSetting updateInterval = sgOptimization.add(new NumberSetting(
            "intervalo de búsqueda",
            "cada cuántos ticks actualizar la búsqueda de bloques",
            10, 1, 80, 1));

    private final NumberSetting movementThreshold = sgOptimization.add(new NumberSetting(
            "umbral de movimiento",
            "distancia que debe moverse el jugador antes de forzar una actualización",
            3.0, 0.5, 10.0, 0.5));

    private final BooleanSetting asyncSearch = sgOptimization.add(new BooleanSetting(
            "búsqueda asíncrona",
            "buscar bloques en un hilo separado (reduce lag)",
            true));

    private final BooleanSetting incrementalUpdates = sgOptimization.add(new BooleanSetting(
            "actualizaciones incrementales",
            "actualizar solo los bloques nuevos / eliminados en lugar de recalcular todo",
            true));


    // colores

    private final ColorSetting lightColor = sgColors.add(new ColorSetting(
            "color de la luz",
            "color de los bloques con luz",
            new Color(0, 255, 0, 67), false));

    private final ColorSetting darknessColor = sgColors.add(new ColorSetting(
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

        onWater.onUpdate(dontCullWater::visibility);
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

    @SuppressWarnings("deprecation")
    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (mc.player == null || mc.level == null) return;

        // actualización periódica
        if (updateTimer++ >= updateInterval.getIntValue()) {
            updateTimer = 0;
            updateBlocks();
        }

        // renderizado
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        int radiusValue = radius.getIntValue();
        double radiusSq = radiusValue * radiusValue;

        // actualizar lista de renderizado basado en distancia
        blocksToRender.clear();
        for (BlockPos pos : cachedBlocks) {
            double distanceSq = pos.distSqr(MiscUtil.vec3iOf(cameraPos));
            if (distanceSq <= radiusSq) {
                blocksToRender.add(pos);
            }
        }

        // renderizar bloques visibles
        for (BlockPos block : blocksToRender) {
            if (!mc.level.hasChunkAt(block)) continue;

            BlockState state = mc.level.getBlockState(block);
            BlockState aboveState = mc.level.getBlockState(block.above());

            if (!isValidSpawnSurface(block, state, aboveState)) continue;

            // nivel de luz
            int light = mc.level.getBrightness(LightLayer.BLOCK, block.above());

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
            boolean shouldCull = !(onWater.getValue() && aboveState.is(Blocks.WATER) && dontCullWater.getValue());
            RenderUtil.drawBlockFaceFilled(event.getMatrices(), block, Direction.UP, color, 0.001f, shouldCull);
        }
    }

    private void updateBlocks() {
        if (mc.player == null || mc.level == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        int currentRadius = radius.getIntValue();
        int currentYRadius = yRadius.getIntValue();

        // verificar si hace falta actualizar
        boolean needsUpdate = fullRescanNeeded ||
                lastRadius != currentRadius ||
                lastYRadius != currentYRadius ||
                (lastPlayerPos != null &&
                        playerPos.distSqr(lastPlayerPos) > (movementThreshold.getValue() * movementThreshold.getValue()));

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
            }, Sputnik.MOD_ID + "-lightoverlay-search").start();
        } else {
            performFullBlockSearch(playerPos, currentRadius, currentYRadius);
        }
    }

    @SuppressWarnings("deprecation")
    private void performFullBlockSearch(BlockPos center, int radius, int yRadius) {
        Set<BlockPos> newBlocks = new HashSet<>();
        int radiusSq = radius * radius;

        int minY = Math.max(mc.level.getMinY(), center.getY() - yRadius);
        int maxY = Math.min(mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mc.player.blockPosition()) - 1, center.getY() + yRadius);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z > radiusSq) continue;

                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = center.offset(x, y - center.getY(), z);

                    // nuh uh si el chunk no está cargado
                    if (!mc.level.hasChunkAt(pos)) continue;

                    BlockState state = mc.level.getBlockState(pos);
                    BlockState aboveState = mc.level.getBlockState(pos.above());

                    if (isValidSpawnSurface(pos, state, aboveState)) {
                        newBlocks.add(pos.immutable());
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
        int minY = Math.max(mc.level.getMinY(), center.getY() - yRadius);
        int maxY = Math.min(mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mc.player.blockPosition()) - 1, center.getY() + yRadius);

        // buscar en una columna específica (x fijo, variar z)
        for (int z = -radius; z <= radius; z++) {
            int xDist = incrementalLayer;
            if (xDist*xDist + z*z > radiusSq) continue;

            for (int y = minY; y <= maxY; y++) {
                BlockPos pos = new BlockPos(currentX, y, center.getZ() + z);

                if (!mc.level.hasChunkAt(pos)) continue;

                BlockState state = mc.level.getBlockState(pos);
                BlockState aboveState = mc.level.getBlockState(pos.above());

                boolean isValid = isValidSpawnSurface(pos, state, aboveState);

                if (isValid) {
                    cachedBlocks.add(pos.immutable());
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
        if (state.isAir() || !state.canOcclude() || !state.isCollisionShapeFullBlock(mc.level, pos)) {
            return false;
        }

        Block aboveBlock = aboveState.getBlock();

        if (aboveState.isAir()) return true;
        if (aboveBlock == Blocks.WATER && onWater.getValue()) return true;
        if (aboveBlock instanceof VegetationBlock) return true;

        if (aboveBlock instanceof SlabBlock ||
                aboveBlock instanceof StairBlock ||
                aboveBlock instanceof ButtonBlock ||
                aboveBlock instanceof CarpetBlock ||
                aboveBlock instanceof PressurePlateBlock ||
                aboveBlock instanceof IronBarsBlock ||
                aboveBlock instanceof TrapDoorBlock ||
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

        return !aboveState.canOcclude() || !aboveState.isCollisionShapeFullBlock(mc.level, pos.above());
    }
}
