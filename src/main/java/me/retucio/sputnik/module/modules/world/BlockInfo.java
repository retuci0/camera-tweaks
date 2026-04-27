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

import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import java.util.Collection;


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
        String json = new Gson().toJson(data);
        String name = mc.level.getBlockState(result.getBlockPos()).getBlock().getDescriptionId();
        Vec3 pos = result.getLocation();
        switch (copy.getValue()) {
            case JSON -> mc.keyboardHandler.setClipboard(json);
            case COMMAND -> mc.keyboardHandler.setClipboard(
                    String.format("/setblock %f %f %f %s %s", pos.x, pos.y, pos.z, name, json));
        }
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
