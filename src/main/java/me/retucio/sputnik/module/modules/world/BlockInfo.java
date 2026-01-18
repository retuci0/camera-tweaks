package me.retucio.sputnik.module.modules.world;

import com.google.gson.Gson;
import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.KeyEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;


// copy summon / setblock command
// copy json data to clipboard
// print data (keybinds)
public class BlockInfo extends Module {

    public KeySetting key = sgGeneral.add(new KeySetting(
            "tecla",
            "tecla a pulsar para obtener la info.",
            GLFW.GLFW_KEY_CAPS_LOCK
    ));

    public EnumSetting<CopyMode> copy = sgGeneral.add(new EnumSetting<>(
            "copiar",
            "copiar al portapapeles",
            CopyMode.class,
            CopyMode.COMMAND
    ));

    public BlockInfo() {
        super("info. de bloque",
                "muestra el contenido nbt del bloque al que se está apuntando",
                Category.WORLD);
    }

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getKey() != key.getValue() || event.getAction() != GLFW.GLFW_PRESS) return;

        HitResult result = mc.player.getCrosshairTarget(
                mc.getRenderTickCounter().getTickProgress(false),
                mc.getCameraEntity()
        );

        if (result == null) {
            ChatUtil.warn("no estás apuntando a ningún bloque");
            return;
        }

        if (result instanceof BlockHitResult blockHitResult) {
            BlockState state = mc.world.getBlockState(blockHitResult.getBlockPos());
            Collection<Property<?>> data = state.getProperties();
            for (Property<?> property : data) {
                ChatUtil.info(property.getName() + ": " + state.get(property));
            }
            copy(blockHitResult, data);
        } else {
            ChatUtil.warn("no estás apuntando a ningún bloque");
        }
    }

    private void copy(BlockHitResult result, Collection<Property<?>> data) {
        String json = new Gson().toJson(data);
        String name = mc.world.getBlockState(result.getBlockPos()).getBlock().getTranslationKey();
        Vec3d pos = result.getPos();
        switch (copy.getValue()) {
            case JSON -> mc.keyboard.setClipboard(json);
            case COMMAND -> mc.keyboard.setClipboard(
                    String.format("/setblock %f %f %f %s %s", pos.x, pos.y, pos.z, name, json));
        }
    }

    public enum CopyMode {
        DONT("no copiar"),
        COMMAND("copiar comando /setblock"),
        JSON("copiar contenido json");

        private final String name;
        CopyMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
