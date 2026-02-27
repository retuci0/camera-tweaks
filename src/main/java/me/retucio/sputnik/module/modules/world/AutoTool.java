package me.retucio.sputnik.module.modules.world;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.event.input.MouseClickEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.InventoryUtil;
import me.retucio.sputnik.util.KeyUtil;
import me.retucio.sputnik.util.MiscUtil;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoTool extends Module {

    private final NumberSetting delaySetting = sgGeneral.add(new NumberSetting(
            "delay",
            "delay al cambiar de herramienta",
            0,
            0,
            20,
            1
    ));

    private final BooleanSetting switchBack = sgGeneral.add(new BooleanSetting(
            "cambiar de vuelta",
            "cambiar de vuelta a la herramienta inicial",
            false
    ));

    private final NumberSetting switchBackDelaySetting = sgGeneral.add(new NumberSetting(
            "delay del cambio de vuelta",
            "delay en ticks para cambiar de vuelta a la herramienta inicial",
            0,
            0,
            20,
            1
    ));

    private boolean startedMining;
    private boolean finishedMining;

    private int delay = 0;
    private int switchBackDelay = 0;

    private int prevSlot = -1;

    public AutoTool() {
        super("auto herramienta", "cambia a la herramienta adecuada automáticamente", Category.WORLD);
        switchBack.onUpdate(v -> switchBackDelaySetting.visibility(v));
    }

    @Override
    public void onEnable() {
        reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }

    private void reset() {
        startedMining = false;
        finishedMining = false;
        delay = 0;
        switchBackDelay = 0;
        prevSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.getInventory() == null) return;

        if (startedMining && delaySetting.getIntValue() > 0) {
            delay++;
            if (delay >= delaySetting.getIntValue()) {
                selectFastestTool();
                startedMining = false;
                delay = 0;
            }
        }

        if (finishedMining && switchBack.getValue() && switchBackDelaySetting.getIntValue() > 0) {
            switchBackDelay++;
            if (switchBackDelay >= switchBackDelaySetting.getIntValue()) {
                switchBack();
                finishedMining = false;
                switchBackDelay = 0;
            }
        }
    }

    @EventListener
    private void onKey(KeyEvent event) {
        handle(event.getKey(), event.getAction());
    }

    @EventListener
    private void onClick(MouseClickEvent event) {
        handle(event.getButton(), event.getAction());
    }

    private void handle(int key, int action) {
        if (mc.player == null || mc.player.getInventory() == null) return;
        if (key != KeyUtil.getKey(mc.options.attackKey)) return;

        if (action == GLFW.GLFW_PRESS) {
            onStartMining();
        } else if (action == GLFW.GLFW_RELEASE) {
            onFinishMining();
        }
    }

    private void onStartMining() {
        finishedMining = false;
        switchBackDelay = 0;

        if (switchBack.getValue() && prevSlot == -1) {
            prevSlot = mc.player.getInventory().getSelectedSlot();
        }

        if (delaySetting.getIntValue() == 0) {
            selectFastestTool();
        } else {
            startedMining = true;
            delay = 0;
        }
    }

    private void onFinishMining() {
        startedMining = false;
        delay = 0;

        if (switchBack.getValue() && prevSlot != -1) {
            if (switchBackDelaySetting.getIntValue() == 0) {
                switchBack();
            } else {
                finishedMining = true;
                switchBackDelay = 0;
            }
        }
    }

    private void selectFastestTool() {
        if (mc.player == null || mc.getCameraEntity() == null || mc.world == null) return;

        HitResult result = mc.getCameraEntity().raycast(mc.player.getBlockInteractionRange(), 0, false);
        if (result.getType() == HitResult.Type.MISS || !(result instanceof BlockHitResult bhr)) return;
        BlockState state = mc.world.getBlockState(bhr.getBlockPos());

        List<ItemStack> tools = InventoryUtil.find(DataComponentTypes.TOOL);
        if (tools.isEmpty()) return;

        Map<Float, ItemStack> speeds = new HashMap<>();
        for (ItemStack tool : tools) {
            ToolComponent toolComponent = tool.get(DataComponentTypes.TOOL);
            if (toolComponent != null) {
                speeds.put(toolComponent.getSpeed(state), tool);
            }
        }

        Float fastestSpeed = MiscUtil.getHighest(speeds.keySet().stream().toList());
        ItemStack fastestTool = speeds.get(fastestSpeed);
        int slot = mc.player.getInventory().getSlotWithStack(fastestTool);

        if (slot != -1) {
            if (switchBack.getValue()) prevSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(slot);
        }
    }

    private void switchBack() {
        if (prevSlot == -1 || prevSlot == mc.player.getInventory().getSelectedSlot()) return;

        mc.player.getInventory().setSelectedSlot(prevSlot);
        prevSlot = -1;
        finishedMining = false;
        switchBackDelay = 0;
    }
}