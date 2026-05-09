package me.retucio.sputnik.module.modules.player;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import me.retucio.sputnik.util.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

// todo: arreglar MLGs con bloques
public class NoFall extends Module {

    private boolean placed, shouldUse;

    private final List<Item> mlgItems = List.of(Items.WATER_BUCKET, Items.ENDER_PEARL,
            Items.POWDER_SNOW_BUCKET, Items.TWISTING_VINES, Items.SLIME_BLOCK,
            Items.HAY_BLOCK, Items.HONEY_BLOCK,  Items.BLACK_BED,
            Items.BLUE_BED, Items.BROWN_BED, Items.CYAN_BED, Items.GRAY_BED,
            Items.GREEN_BED, Items.LIGHT_BLUE_BED, Items.LIGHT_GRAY_BED, Items.LIME_BED,
            Items.MAGENTA_BED, Items.ORANGE_BED, Items.PINK_BED, Items.PURPLE_BED,
            Items.RED_BED, Items.WHITE_BED, Items.YELLOW_BED);

    private final List<Block> safeBlocks = List.of(
            Blocks.WATER, Blocks.SLIME_BLOCK, Blocks.HAY_BLOCK, Blocks.HONEY_BLOCK,
            Blocks.TWISTING_VINES, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED,
            Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED,
            Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED,
            Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.RED_BED, Blocks. WHITE_BED,
            Blocks.YELLOW_BED);

    private final EnumSetting<NoFallMode> mode = sgGeneral.add(new EnumSetting<>(
            "modo",
            "de qué manera evitar el daño de caída",
            NoFallMode.class, NoFallMode.PACKET
    ));

    private final ListSetting<Item> items = sgGeneral.add(new ListSetting<>(
            "items",
            "items con los que amortiguar la caída",
            mlgItems,
            Lists.allTrue(mlgItems)
    ));

    private final NumberSetting distance = sgGeneral.add(new NumberSetting(
            "distancia",
            "distancia mínima de caída para aplicar la amortiguación",
            3,
            3,
            23,
            1
    ));

    private final BooleanSetting clutch = sgGeneral.add(new BooleanSetting(
            "salvada",
            "cambiar a modo paquete si no se encuentra un cubo disponible",
            true
    ));

    private final BooleanSetting center = sgGeneral.add(new BooleanSetting(
            "centrar jugador",
            "centrar jugador respecto al bloque que tenga debajo",
            false
    ));

    public NoFall() {
        super("tobillos fuertes", "evita el daño de caída", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null
                || mc.level == null
                || mc.getCameraEntity() == null
                || mc.gameMode == null)
            return;

        if (mc.player.onGround()) {
            placed = false;
            shouldUse = false;
        }

        if (shouldUse) {
            mc.startUseItem();
        }

        if (mode.is(NoFallMode.CLUTCH)
                && mc.player.fallDistance >= distance.getValue()
                && !mc.player.onGround()
                && !placed) {

            if (center.getValue()) {
                Vec3 diff = mc.player.position().subtract(mc.player.blockPosition().getCenter());
                if (diff.lengthSqr() > 10e-3) {
                    Vec3 impulse = new Vec3(-diff.x * 0.05, 0, -diff.z * 0.05);
                    mc.player.push(impulse.x, impulse.y, impulse.z);
                }
            }

            HitResult result = mc.getCameraEntity().pick(3, 0, true);
            if (result instanceof BlockHitResult bhr) {
                BlockPos pos = bhr.getBlockPos();
                while (mc.level.getBlockState(pos).is(Blocks.AIR)) {
                    pos = pos.below();
                }
                if (isSafeBlock(pos)) return;

                // encontrar ítem
                ItemStack mlgItem = getMlgItem();
                if (mlgItem == null) {
                    if (!placed) {
                        ChatUtil.warn("amortiguador (agua, etc.) no encontrado");
                        placed = true;
                        if (clutch.getValue()) {
                            mode.setValue(NoFallMode.PACKET);
                            ChatUtil.info("cambiado a modo de paquetes");
                        }
                    }
                    return;
                }

                // mirar abajo
                mc.player.setXRot(180);

                int slot = mc.player.getInventory().findSlotMatchingItem(mlgItem);
                mc.player.getInventory().setSelectedSlot(slot);

                // usar bloque
                if (mlgItem.getItem() instanceof BlockItem) {
                    shouldUse = true;
                    placed = true;
                    return;
                }

                // usar ítem
                InteractionResult didPlace = mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                if (didPlace.consumesAction())
                    placed = true;
            }
        }
    }

    @EventListener
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null
                || mc.getConnection() == null
                || !mode.is(NoFallMode.PACKET)
                || mc.player.getAbilities().instabuild
                || mc.player.fallDistance < distance.getValue())
            return;

        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet) {
            if (!packet.isOnGround()) {
                if (mc.player.fallDistance >= 3) {
                    event.cancel();
                    sendOnGroundPacket();
                }
            }
        }
    }

    private void sendOnGroundPacket() {
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ(),
                mc.player.getYRot(),
                mc.player.getXRot(),
                true,
                mc.player.horizontalCollision
        ));
    }

    private ItemStack getMlgItem() {
        for (Item item : items.getEnabledOptions()) {
            ItemStack stack = InventoryUtil.findStack(itemStack -> itemStack.is(item));
            if (stack != null && Inventory.isHotbarSlot(mc.player.getInventory().findSlotMatchingItem(stack))) {
                return stack;
            }
        }
        return null;
    }

    private boolean isSafeBlock(BlockPos pos) {
        return safeBlocks.contains(mc.level.getBlockState(pos).getBlock());
    }

    private enum NoFallMode {
        CLUTCH("waterdrop"),
        PACKET("paquetes");

        private final String name;
        NoFallMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
