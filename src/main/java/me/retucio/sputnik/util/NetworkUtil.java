package me.retucio.sputnik.util;


import me.retucio.sputnik.event.network.PacketEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class NetworkUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

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

    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket packet) {
            long currentWorldTime = packet.time();
            long currentRealTime = System.currentTimeMillis();

            if (lastWorldTime != -1L && lastRealTime != -1L) {
                long elapsedRealTime = currentRealTime - lastRealTime;
                long elapsedWorldTicks = currentWorldTime - lastWorldTime;

                if (elapsedRealTime > 0) {
                    float tps = (float) elapsedWorldTicks / (elapsedRealTime / 1000.0f);
                    tps = Math.max(0.1f, Math.min(20.0f, tps));
                    updateTPS(tps);
                }
            }

            lastWorldTime = currentWorldTime;
            lastRealTime = currentRealTime;
        }
    }


    public static void sendPacketNoEvent(Packet<?> packet) {
        mc.getNetworkHandler().getConnection().sendImmediately(packet, null, true);
    }

    public static void receivePacketNoEvent(Packet<?> packet) {
        receivePacketNoEvent(packet, mc.getNetworkHandler().getConnection().getPacketListener());
    }

    public static void receivePacketNoEvent(Packet<?> packet, PacketListener listener) {
        ClientConnection.handlePacket(packet, listener);
    }

    public static void interactBlock(BlockHitResult blockHitResult, Hand hand, boolean swing) {
        boolean wasSneaking = mc.player.isSneaking();
        mc.player.setSneaking(false);

        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, blockHitResult);

        if (result.isAccepted()) {
            if (swing) mc.player.swingHand(hand);
            else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }

        mc.player.setSneaking(wasSneaking);
    }

    public static boolean placeBlock(BlockPos blockPos, Hand hand, int slot, boolean rotate, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (slot < 0 || slot > 8) return false;

        Block toPlace = Blocks.OBSIDIAN;
        ItemStack i = hand == Hand.MAIN_HAND ? mc.player.getInventory().getStack(slot) : mc.player.getOffHandStack();
        if (i.getItem() instanceof BlockItem blockItem) toPlace = blockItem.getBlock();
        if (!canPlaceBlock(blockPos, checkEntities, toPlace)) return false;

        Vec3d hitPos = Vec3d.ofCenter(blockPos);

        BlockPos neighbour;
        Direction side = getClosestSide(blockPos);

        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);

        int prevSlot = mc.player.getInventory().getSelectedSlot();

        if (rotate) {
            mc.player.setYaw((float) EntityUtil.getYaw(hitPos));
            mc.player.setPitch((float) EntityUtil.getPitch(hitPos));
        }

        InventoryUtil.swapWithHotbar(mc.player.getInventory().getSelectedSlot(), slot);
        interactBlock(bhr, hand, swingHand);

        if (swapBack) {
            InventoryUtil.swapWithHotbar(mc.player.getInventory().getSelectedSlot(), prevSlot);
        }

        return true;
    }

    public static boolean canPlaceBlock(BlockPos blockPos, boolean checkEntities, Block block) {
        if (blockPos == null) return false;
        if (!World.isValid(blockPos)) return false;
        if (!mc.world.getBlockState(blockPos).isReplaceable()) return false;
        return !checkEntities || mc.world.canPlace(block.getDefaultState(), blockPos, ShapeContext.absent());
    }

    public static Direction getClosestSide(BlockPos blockPos) {
        Vec3d lookVec = blockPos.toCenterPos().subtract(mc.player.getEyePos());
        double bestRelevancy = -Double.MAX_VALUE;
        Direction bestSide = null;

        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = mc.world.getBlockState(neighbor);

            if (state.isAir()) continue;

            if (!state.getFluidState().isEmpty()) continue;

            double relevancy = side.getAxis().choose(lookVec.getX(), lookVec.getY(), lookVec.getZ()) * side.getDirection().offset();
            if (relevancy > bestRelevancy) {
                bestRelevancy = relevancy;
                bestSide = side;
            }
        }

        return bestSide;
    }
}