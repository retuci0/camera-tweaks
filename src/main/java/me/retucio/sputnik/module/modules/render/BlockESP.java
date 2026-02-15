package me.retucio.sputnik.module.modules.render;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.Lists;
import me.retucio.sputnik.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class BlockESP extends Module {

    private final SettingGroup sgSearch = addSg(new SettingGroup("búsqueda", true));
    private final SettingGroup sgOulines = addSg(new SettingGroup("contorno", true));
    private final SettingGroup sgFilling = addSg(new SettingGroup("relleno", true));

    // bloques
    private final ListSetting<Block> blocks = sgGeneral.add(new ListSetting<>(
            "bloques", "bloques a resaltar",
            Lists.blockList,
            Lists.allFalse(Lists.blockList),
            Lists.blockNames
    ));

    private final NumberSetting maxBlocks = sgGeneral.add(new NumberSetting(
            "máx. bloques", "número máximo de bloques a resaltar",
            500, 1, 2000, 1
    ));


    // búsqueda
    private final NumberSetting radius = sgSearch.add(new NumberSetting(
            "radio", "radio de búsqueda de bloques",
            24, 2, 128, 1
    ));

    private final NumberSetting searchInterval = sgSearch.add(new NumberSetting(
            "intervalo", "intervalo de búsqueda en ticks",
            10, 1, 40, 1
    ));

    private final BooleanSetting asyncSearch = sgSearch.add(new BooleanSetting(
            "búsqueda asíncrona", "busca los bloques en un hilo separado (optimización)",
            true
    ));

    private final NumberSetting distanceToMove = sgSearch.add(new NumberSetting(
            "distancia a moverse", "distancia a moverse antes de actualizar los bloques",
            3, 0, 10, 0.1
    ));


    // contorno
    private final BooleanSetting outlines = sgOulines.add(new BooleanSetting(
            "contorno", "renderizar contorno de bloques",
            true
    ));

    private final ColorSetting outlineColor = sgOulines.add(new ColorSetting(
            "color del contorno", "color del contorno",
            Colors.CELESTE, false
    ));

    private final NumberSetting lineWidth = sgOulines.add(new NumberSetting(
            "grosor del contorno", "grosor de las líneas del contorno",
            1, 1, 5, 0.2
    ));


    // relleno
    private final BooleanSetting fillings = sgFilling.add(new BooleanSetting(
            "relleno", "renderizar relleno de bloques",
            true
    ));

    private final ColorSetting fillingColor = sgFilling.add(new ColorSetting(
            "color del relleno", "color del relleno",
            new Color(57, 177, 215, 67), false
    ));

    // caché
    private final Map<BlockPos, VoxelShape> cachedBlocks = new ConcurrentHashMap<>();
    private final Set<Block> enabledBlocks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private BlockPos lastPlayerPos = null;
    private int lastRadius = -1;
    private int searchTimer = 0;
    private boolean isSearching = false;


    public BlockESP() {
        super("resaltado de bloques",
                "te muestra la posición de cierto tipo de bloque renderizando una caja con su forma",
                Category.RENDER);

        blocks.onUpdate(v ->
                updateEnabledBlocks()
        );

        outlines.onUpdate(v -> {
            outlineColor.setVisible(v);
            lineWidth.setVisible(v);
        });

        fillings.onUpdate(v ->
                fillingColor.setVisible(v)
        );
    }

    @Override
    public void onEnable() {
        cachedBlocks.clear();
        enabledBlocks.clear();
        updateEnabledBlocks();
        searchTimer = 0;

        super.onEnable();
    }

    @Override
    public void onDisable() {
        cachedBlocks.clear();
        enabledBlocks.clear();

        super.onDisable();
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        // aplicar el intervalo
        if (searchTimer++ >= searchInterval.getIntValue()) {
            searchTimer = 0;
            searchBlocks();
        }

        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        int rendered = 0;
        Iterator<Map.Entry<BlockPos, VoxelShape>> iterator = cachedBlocks.entrySet().iterator();

        while (iterator.hasNext() && rendered < maxBlocks.getIntValue()) {
            Map.Entry<BlockPos, VoxelShape> entry = iterator.next();
            BlockPos pos = entry.getKey();
            VoxelShape shape = entry.getValue();

            if (!isValidBlock(pos)) {
                iterator.remove();
                continue;
            }

            // validación de distancia
            double distance = Math.sqrt(pos.getSquaredDistance(cameraPos));
            if (distance > radius.getIntValue()) {
                iterator.remove();
                continue;
            }

            // renderizado
            if (outlines.getValue()) {
                Color color = outlineColor.getValue();
                RenderUtil.drawVoxelShapeOutline(
                        event.getMatrices(),
                        shape,
                        pos,
                        color,
                        lineWidth.getFloatValue(),
                        false);
            }

            if (fillings.getValue()) {
                Color color = fillingColor.getValue();
                RenderUtil.drawVoxelShapeFilled(
                        event.getMatrices(),
                        shape,
                        pos,
                        color,
                        false);
            }

            rendered++;
        }
    }

    private void updateEnabledBlocks() {
        enabledBlocks.clear();
        enabledBlocks.addAll(blocks.getEnabledOptions());
    }

    private void searchBlocks() {
        if (mc.player == null || mc.world == null || enabledBlocks.isEmpty()) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int currentRadius = radius.getIntValue();

        // si el jugador no se ha movido mucho, omitir la búsqueda
        if (lastPlayerPos != null && lastRadius == currentRadius &&
                playerPos.getSquaredDistance(lastPlayerPos) < (distanceToMove.getValue() * distanceToMove.getIntValue())) {
            return;
        }

        lastPlayerPos = playerPos;
        lastRadius = currentRadius;

        // multithreading
        if (asyncSearch.getValue()) {
            if (isSearching) return;

            isSearching = true;
            new Thread(() -> {
                try {
                    performBlockSearch(playerPos, currentRadius);
                } finally {
                    isSearching = false;
                }
            }, "sputnik-blockESP-search").start();
        } else {
            performBlockSearch(playerPos, currentRadius);
        }
    }

    // buscar los bloques
    private void performBlockSearch(BlockPos center, int radius) {
        Map<BlockPos, VoxelShape> newBlocks = new HashMap<>();
        int radiusSq = radius * radius;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + y*y + z*z > radiusSq) continue;

                    BlockPos pos = center.add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();

                    if (enabledBlocks.contains(block)) {
                        VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
                        if (!shape.isEmpty()) {
                            newBlocks.put(pos.toImmutable(), shape);
                        }
                    }
                }
            }
        }

        // actualizar caché
        cachedBlocks.keySet().retainAll(newBlocks.keySet());
        cachedBlocks.putAll(newBlocks);
    }

    // verificar que el bloque sea válido
    @SuppressWarnings("deprecation")
    private boolean isValidBlock(BlockPos pos) {
        if (mc.world == null || !mc.world.isChunkLoaded(pos)) return false;
        Block block = mc.world.getBlockState(pos).getBlock();
        return enabledBlocks.contains(block);
    }
}
