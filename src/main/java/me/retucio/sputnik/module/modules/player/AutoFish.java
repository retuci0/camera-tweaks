package me.retucio.sputnik.module.modules.player;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.interact.SwitchSlotEvent;
import me.retucio.sputnik.mixin.accessors.FishingHookAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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
    )).visibility(onlyRecastIfLookingAtWater::getValue);

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
            recastDelay.visibility(v);
            onlyRecastIfLookingAtWater.visibility(v);
            recastRaycastDistance.visibility(v);
        });
    }

    @Override
    public void onDisable() {
        delay = -1;
        shouldRecast = false;
        warned = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getCameraEntity() == null || mc.level == null) return;

        if (delay >= 0) delay++;

        if (mc.player.isHolding(Items.FISHING_ROD) && mc.player.fishing == null && autoRecast.getValue()) {
            HitResult result = mc.getCameraEntity().pick(recastRaycastDistance.getIntValue(), 0, true);

            if (onlyRecastIfLookingAtWater.getValue()) {
                if (result instanceof BlockHitResult bhr && bhr.getType().equals(HitResult.Type.BLOCK)) {
                    if (mc.level.getBlockState(bhr.getBlockPos()).is(Blocks.WATER) && delay >= recastDelay.getIntValue()) {
                        useFishingRod();
                        return;
                    }
                }
            } else {
                useFishingRod();
                return;
            }
        }

        if (mc.player.fishing == null) {
            if (delay >= recastDelay.getIntValue() && shouldRecast) {
                useFishingRod();
            }
            return;
        }

        FishingHookAccessor fishHook = (FishingHookAccessor) mc.player.fishing;

        if (fishHook.caughtFish()) {
            useFishingRod();
        }
    }

    @EventListener
    public void onSwitchSlot(SwitchSlotEvent event) {
        warned = false;
    }

    private void useFishingRod() {
        int slot = switchToFishingRod();
        if (slot == -1) return;

        ItemStack rod = mc.player.getInventory().getItem(slot);
        if (rod.getDamageValue() >= rod.getMaxDamage() - 1 && dontBreak.getValue()) {
            if (!warned) {
                ChatUtil.warn("caña a punto de romperse");
                warned = true;
            }
            return;
        }

        mc.startUseItem();
        delay = 0;
        shouldRecast = false;
    }

    private int switchToFishingRod() {
        if (mc.player.getActiveItem().is(Items.FISHING_ROD)) return mc.player.getInventory().getSelectedSlot();
        List<Integer> slots = InventoryUtil.findAllSlots(stack -> stack.is(Items.FISHING_ROD));
        for (int slot : slots) {
            if (Inventory.isHotbarSlot(slot)) {
                mc.player.getInventory().setSelectedSlot(slot);
                return slot;
            }
        }
        return -1;
    }
}