package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.interact.SwitchSlotEvent;
import me.retucio.sputnik.mixin.accessors.FishingBobberEntityAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;

// todo: catch delay, seleccionar la mejor caña
public class AutoFish extends Module {

    private final SettingGroup sgRecast = addSg(new SettingGroup("recast", true));

    private final BooleanSetting autoRecast = sgRecast.add(new BooleanSetting(
            "recast",
            "relanzar la caña automáticamente al pescar algo",
            true
    ));

    private final NumberSetting recastDelay = sgRecast.add(new NumberSetting(
            "delay ",
            "delay entre que se pesca un pez y se vuelve a lanzar la caña",
            5,
            0,
            20,
            1
    ));

    private final BooleanSetting onlyRecastIfLookingAtWater = sgRecast.add(new BooleanSetting(
            "solo si mirando al agua",
            "solamente hacer recast si se está mirando al agua",
            true
    ));

    private final NumberSetting recastRaycastDistance = sgRecast.add(new NumberSetting(
            "distancia del agua",
            "distancia máxima a la que puede estar el agua al que estás mirando",
            10,
            0,
            64,
            1
    ));

    private final BooleanSetting dontBreak = sgGeneral.add(new BooleanSetting(
            "evitar romper",
            "evitar romper la caña si está en las últimas",
            true
    ));

    private int delay = -1;
    private boolean shouldRecast;
    private boolean warned;

    public AutoFish() {
        super("auto pesca", "pesca por ti", Category.PLAYER);
        autoRecast.onUpdate(v -> {
            recastDelay.setVisible(v);
            onlyRecastIfLookingAtWater.setVisible(v);
            recastRaycastDistance.setVisible(v);
        });

        onlyRecastIfLookingAtWater.onUpdate(v -> recastRaycastDistance.setVisible(v));
    }

    @Override
    public void onDisable() {
        delay = -1;
        shouldRecast = false;
        warned = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getCameraEntity() == null) return;

        if (delay >= 0) delay++;

        if (mc.player.isHolding(Items.FISHING_ROD) && mc.player.fishHook == null && autoRecast.getValue()) {
            HitResult result = mc.getCameraEntity().raycast(recastRaycastDistance.getIntValue(), 0, true);

            if (onlyRecastIfLookingAtWater.getValue()) {
                if (result instanceof BlockHitResult bhr && bhr.getType().equals(HitResult.Type.BLOCK)) {
                    if (mc.world.getBlockState(bhr.getBlockPos()).isOf(Blocks.WATER) && delay >= recastDelay.getIntValue()) {
                        useFishingRod();
                        return;
                    }
                }
            } else {
                useFishingRod();
                return;
            }
        }

        if (mc.player.fishHook == null) {
            if (delay >= recastDelay.getIntValue() && shouldRecast) {
                useFishingRod();
            }
            return;
        }

        FishingBobberEntityAccessor fishHook = (FishingBobberEntityAccessor) mc.player.fishHook;

        if (fishHook.caughtFish()) {
            useFishingRod();
        }
    }

    @SubscribeEvent
    public void onSwitchSlot(SwitchSlotEvent event) {
        warned = false;
    }

    private void useFishingRod() {
        int slot = switchToFishingRod();
        if (slot == -1) return;

        ItemStack rod = mc.player.getInventory().getStack(slot);
        if (rod.getDamage() >= rod.getMaxDamage() - 1 && dontBreak.getValue()) {
            if (!warned) {
                ChatUtil.warn("caña a punto de romperse");
                warned = true;
            }
            return;
        }

        mc.doItemUse();
        delay = 0;
        shouldRecast = false;
    }

    private int switchToFishingRod() {
        if (mc.player.getActiveItem().isOf(Items.FISHING_ROD)) return mc.player.getInventory().getSelectedSlot();
        List<Integer> slots = InventoryUtil.findItem(Items.FISHING_ROD);
        for (int slot : slots) {
            if (PlayerInventory.isValidHotbarIndex(slot)) {
                mc.player.getInventory().setSelectedSlot(slot);
                return slot;
            }
        }
        return -1;
    }
}