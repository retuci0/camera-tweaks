package me.retucio.sputnik.util;


import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;


public class NetworkUtil {

    private static final Minecraft mc = Minecraft.getInstance();


    // tps

    private static final List<Float> tpsHistory = new ArrayList<>();
    private static float estimatedTPS = 20f;
    private static long lastWorldTime = -1L;
    private static long lastRealTime = -1L;

    public static float getTPS() {
        return estimatedTPS;
    }

    public static void updateTPS(float tps) {
        tpsHistory.add(tps);
        if (tpsHistory.size() > 10)
            tpsHistory.removeFirst();


        float sum = 0;
        for (float t : tpsHistory) sum += t;
        estimatedTPS = sum / tpsHistory.size();
    }

    @EventListener
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket packet) {
            long currentWorldTime = packet.gameTime();
            long currentRealTime = System.currentTimeMillis();

            if (lastWorldTime != -1L && lastRealTime != -1L) {
                long elapsedRealTime = currentRealTime - lastRealTime;
                long elapsedWorldTicks = currentWorldTime - lastWorldTime;

                if (elapsedRealTime > 0) {
                    float tps = (float) elapsedWorldTicks / (elapsedRealTime / 1000.0f);
                    tps = Math.clamp(tps, 0.1f, 20.0f);
                    updateTPS(tps);
                }
            }

            lastWorldTime = currentWorldTime;
            lastRealTime = currentRealTime;
        }
    }



    public static void sendPacketNoEvent(Packet<?> packet) {
        mc.getConnection().getConnection().sendPacket(packet, null, true);
    }

    public static void receivePacketNoEvent(Packet<?> packet) {
        receivePacketNoEvent(packet, mc.getConnection().getConnection().getPacketListener());
    }

    public static void receivePacketNoEvent(Packet<?> packet, PacketListener listener) {
        Connection.genericsFtw(packet, listener);
    }


    // interactuar con un bloque mediante MultiPlayerGameMode
    public static void interactBlock(BlockHitResult blockHitResult, InteractionHand hand, boolean swing) {
        boolean wasSneaking = mc.player.isCrouching();
        mc.player.setShiftKeyDown(false);

        InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, blockHitResult);

        if (result.consumesAction()) {
            if (swing) mc.player.swing(hand);
            else mc.getConnection().send(new ServerboundSwingPacket(hand));
        }

        mc.player.setShiftKeyDown(wasSneaking);
    }

    // colocar un bloque
    public static boolean placeBlock(BlockPos blockPos, InteractionHand hand, int slot,
                                     boolean rotateClient, boolean rotateServer, boolean swingHand,
                                     boolean checkEntities, boolean swapBack)
    {
        if (slot < 0 || slot > 8 || mc.player == null) return false;

        Block toPlace = Blocks.OBSIDIAN;
        ItemStack stack = hand == InteractionHand.MAIN_HAND ? mc.player.getInventory().getItem(slot) : mc.player.getOffhandItem();
        if (stack.getItem() instanceof BlockItem blockItem) toPlace = blockItem.getBlock();
        if (!canPlaceBlock(blockPos, checkEntities, toPlace)) return false;

        Vec3 hitPos = Vec3.atCenterOf(blockPos);

        BlockPos neighbour;
        Direction side = getPlaceSide(blockPos);

        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.relative(side);
            hitPos = hitPos.add(side.getStepX() * 0.5, side.getStepY() * 0.5, side.getStepZ() * 0.5);
        }

        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);
        int prevSlot = mc.player.getInventory().getSelectedSlot();

        if (rotateClient) EntityUtil.lookAtClient(hitPos);
        if (rotateServer) EntityUtil.lookAtServer(hitPos);

        InventoryUtil.swapWithHotbar(mc.player.getInventory().getSelectedSlot(), slot);
        interactBlock(bhr, hand, swingHand);

        if (swapBack) {
            InventoryUtil.swapWithHotbar(mc.player.getInventory().getSelectedSlot(), prevSlot);
        }

        return true;
    }

    // retorna verdadero si puede colocar un bloque de tipo `block` en `blockPos`
    public static boolean canPlaceBlock(BlockPos blockPos, boolean checkEntities, Block block) {
        if (blockPos == null) return false;
        if (!Level.isInSpawnableBounds(blockPos)) return false;
        if (!mc.level.getBlockState(blockPos).canBeReplaced()) return false;
        return !checkEntities || mc.level.isUnobstructed(block.defaultBlockState(), blockPos, CollisionContext.empty());
    }


    // retorna la dirección desde la que el jugador colocará bloques
    public static Direction getPlaceSide(BlockPos blockPos) {
        Vec3 lookVec = blockPos.getCenter().subtract(mc.player.getEyePosition());
        double bestRelevancy = -Double.MAX_VALUE;
        Direction bestSide = null;

        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.relative(side);
            BlockState state = mc.level.getBlockState(neighbor);

            if (state.isAir() || isInteractableBlock(state.getBlock())) continue;

            if (!state.getFluidState().isEmpty()) continue;

            double relevancy = side.getAxis().choose(lookVec.x(), lookVec.y(), lookVec.z()) * side.getAxisDirection().getStep();
            if (relevancy > bestRelevancy) {
                bestRelevancy = relevancy;
                bestSide = side;
            }
        }

        return bestSide;
    }

    // retorna verdadero si es un bloque interactuable
    public static boolean isInteractableBlock(Block block) {
        return block instanceof CraftingTableBlock
                || block instanceof AnvilBlock
                || block instanceof LoomBlock
                || block instanceof CartographyTableBlock
                || block instanceof GrindstoneBlock
                || block instanceof StonecutterBlock
                || block instanceof ButtonBlock
                || block instanceof BasePressurePlateBlock
                || block instanceof BaseEntityBlock
                || block instanceof BedBlock
                || block instanceof FenceGateBlock
                || block instanceof DoorBlock
                || block instanceof NoteBlock
                || block instanceof TrapDoorBlock;
    }
}