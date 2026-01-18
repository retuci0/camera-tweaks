package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


// https://github.com/etianl/Trouser-Streak/blob/ce27abd30435ca2b43a5e5546f5b070c07c6291a/src/main/java/pwn/noobs/trouserstreak/modules/MaceKill.java
public class MaceKill extends Module {

    public BooleanSetting max = sgGeneral.add(new BooleanSetting(
            "máxima potencia",
            "simular caída desde el hueco con aire más cercano en un rango de 170 bloques",
            false
    ));

    public NumberSetting height = sgGeneral.add(new NumberSetting(
            "altura",
            "altura de la que simular la caída",
            22,
            1,
            170,
            1
    ));

    public BooleanSetting preventDeath = sgGeneral.add(new BooleanSetting(
            "prevenir muerte",
            "prevenir la muerte por daño de caída si el ataque fue bloqueado, cancelando los paquetes de movimiento",
            true
    ));

    public MaceKill() {
        super("estampaescrotos", "estampa escrotos ajenos", Category.PLAYER);
        max.onUpdate(v -> height.setVisible(!v));
    }

    @SubscribeEvent
    @SuppressWarnings("ConstantConditions")
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (mc.player.getMainHandStack().getItem() == Items.MACE
                && event.getPacket() instanceof PlayerInteractEntityC2SPacket packet
                && packet.type.getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            try {
                if (mc.world.getEntityById(packet.entityId) instanceof LivingEntity target) {

                    if (preventDeath.getValue()
                            && (target.isBlocking()
                            || target.isInvulnerable()
                            || target.isInCreativeMode()
                    )) {
                        return;
                    }

                    Vec3d previouspos = mc.player.getEntityPos();
                    int blocks = getMaxHeightAbovePlayer();

                    int packetsRequired = (int) (double) Math.abs(blocks / 10);

                    if (packetsRequired > 20) {
                        packetsRequired = 1;
                    }

                    BlockPos gap1 = (mc.player.getBlockPos().add(0, blocks, 0));
                    BlockPos gap2 = (mc.player.getBlockPos().add(0, blocks + 1, 0));

                    if (isSafeBlock(gap1) && isSafeBlock(gap2)) {
                        if (blocks <= 22) {
                            if (mc.player.hasVehicle()) {
                                for (int i = 0; i < 4; i++) {
                                    mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
                                }

                                double maxHeight = Math.min(mc.player.getVehicle().getY() + 22, mc.player.getVehicle().getY() + blocks);

                                mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(), maxHeight + blocks, mc.player.getVehicle().getZ());
                                mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));

                                mc.player.getVehicle().setPosition(previouspos);
                                mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
                            } else {
                                for (int i = 0; i < 4; i++) {
                                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision));
                                }

                                double maxHeight = Math.min(mc.player.getY() + 22, mc.player.getY() + blocks);

                                PlayerMoveC2SPacket movePacket = new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), maxHeight, mc.player.getZ(), false, mc.player.horizontalCollision);
                                mc.player.networkHandler.sendPacket(movePacket);

                                PlayerMoveC2SPacket returnPacket = new PlayerMoveC2SPacket.PositionAndOnGround(previouspos.getX(), previouspos.getY(), previouspos.getZ(), false, mc.player.horizontalCollision);
                                mc.player.networkHandler.sendPacket(returnPacket);
                            }
                        } else {
                            if (mc.player.hasVehicle()) {
                                for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                                    mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
                                }

                                mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(), mc.player.getVehicle().getY() + blocks, mc.player.getVehicle().getZ());

                                double maxHeight = mc.player.getVehicle().getY() + blocks;

                                mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(), maxHeight + blocks, mc.player.getVehicle().getZ());
                                mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));

                                mc.player.getVehicle().setPosition(previouspos);
                                mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
                            } else {
                                for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, mc.player.horizontalCollision));
                                }

                                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ(), false, mc.player.horizontalCollision));
                                PlayerMoveC2SPacket movePacket = new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ(), false, mc.player.horizontalCollision);
                                mc.player.networkHandler.sendPacket(movePacket);
                            }

                            if (mc.player.hasVehicle()) {
                                mc.player.getVehicle().setPosition(previouspos);
                                mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));

                                mc.player.getVehicle().setPosition(previouspos);
                                mc.player.networkHandler.sendPacket(VehicleMoveC2SPacket.fromVehicle(mc.player.getVehicle()));
                            } else {
                                double maxHeight = mc.player.getY() + blocks;

                                PlayerMoveC2SPacket movePacket = new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), maxHeight, mc.player.getZ(), false, mc.player.horizontalCollision);
                                PlayerMoveC2SPacket returnPacket = new PlayerMoveC2SPacket.PositionAndOnGround(previouspos.getX(), previouspos.getY(), previouspos.getZ(), false, mc.player.horizontalCollision);

                                mc.player.networkHandler.sendPacket(returnPacket);
                                mc.player.networkHandler.sendPacket(movePacket);
                                mc.player.networkHandler.sendPacket(returnPacket);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                ChatUtil.error("upsi: " + e.getMessage());
            }
        }
    }

    private int getMaxHeightAbovePlayer() {
        BlockPos playerPos = mc.player.getBlockPos();
        int maxHeight = playerPos.getY() + (max.getValue() ? 170 : height.getIntValue());

        for (int i = maxHeight; i > playerPos.getY(); i--) {
            BlockPos isopenair1 = new BlockPos(playerPos.getX(), i, playerPos.getZ());
            BlockPos isopenair2 = isopenair1.up(1);

            if (isSafeBlock(isopenair1) && isSafeBlock(isopenair2)) {
                return i - playerPos.getY();
            }
        }

        return 0;
    }

    private boolean isSafeBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).isReplaceable()
                && mc.world.getFluidState(pos).isEmpty()
                && !mc.world.getBlockState(pos).isOf(Blocks.POWDER_SNOW);
    }
}
