package me.retucio.sputnik.module.modules.world;

import com.github.retucio.neutrino.EventListener;
import com.google.gson.Gson;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.util.ChatUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class BlockInfo extends Module {

    private final KeySetting key = sgGeneral.add(new KeySetting(
            "tecla",
            "tecla a pulsar para obtener la info.",
            GLFW.GLFW_KEY_CAPS_LOCK
    ));

    private final EnumSetting<CopyMode> copy = sgGeneral.add(new EnumSetting<>(
            "copiar",
            "copiar al portapapeles",
            CopyMode.class,
            CopyMode.COMMAND
    ));

    private final BooleanSetting withLiquids = sgGeneral.add(new BooleanSetting(
            "líquidos",
            "también incluir líquidos en el raycast",
            false
    ));

    public BlockInfo() {
        super("info. de bloque",
                "muestra el contenido nbt del bloque al que se está apuntando",
                Category.WORLD);
    }

    @EventListener
    private void onKey(KeyEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (event.getKey() != key.getValue() || event.getAction() != GLFW.GLFW_PRESS) return;

        HitResult result = mc.player.pick(
                mc.player.blockInteractionRange(),
                mc.getDeltaTracker().getGameTimeDeltaTicks(),
                withLiquids.getValue()
        );

        if (result.getType() == HitResult.Type.MISS) {
            ChatUtil.warn("no estás apuntando a ningún bloque");
            return;
        }

        if (result instanceof BlockHitResult blockHitResult) {
            BlockState state = mc.level.getBlockState(blockHitResult.getBlockPos());
            Collection<Property<?>> data = state.getProperties();
            for (Property<?> property : data) {
                ChatUtil.info(property.getName() + ": " + state.getValue(property));
            }
            copy(blockHitResult, data);
        } else {
            ChatUtil.warn("no estás apuntando a ningún bloque");
        }
    }

    private void copy(BlockHitResult result, Collection<Property<?>> data) {
        BlockPos pos = result.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        Map<String, String> propertyMap = new LinkedHashMap<>();
        for (Property<?> property : data) {
            propertyMap.put(property.getName(), state.getValue(property).toString());
        }

        if (propertyMap.isEmpty()) {
            ChatUtil.warn("no hay propiedades que copiar para este bloque");
            return;
        }

        String json = new Gson().toJson(propertyMap);
        switch (copy.getValue()) {
            case JSON -> mc.keyboardHandler.setClipboard(json);
            case COMMAND -> mc.keyboardHandler.setClipboard(
                    String.format("/setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), formatBlockState(state))
            );
        }
    }

    private String formatBlockState(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        StringBuilder sb = new StringBuilder(id.toString());

        Collection<Property<?>> props = state.getProperties();
        if (!props.isEmpty()) {
            sb.append("[");
            boolean first = true;
            for (Property<?> prop : props) {
                if (!first) sb.append(",");
                sb.append(prop.getName()).append("=").append(state.getValue(prop).toString());
                first = false;
            }
            sb.append("]");
        }
        return sb.toString();
    }
    private enum CopyMode {
        DONT("no copiar"),
        COMMAND("copiar comando /setblock"),
        JSON("copiar contenido json");

        private final String name;
        CopyMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
