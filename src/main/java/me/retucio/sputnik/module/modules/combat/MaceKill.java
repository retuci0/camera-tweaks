package me.retucio.sputnik.module.modules.combat;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;


/**
 * @link <a href="https://github.com/etianl/Trouser-Streak/blob/ce27abd30435ca2b43a5e5546f5b070c07c6291a/src/main/java/pwn/noobs/trouserstreak/modules/MaceKill.java">créditos a etianl</a>
 *
 * @"author" retucio
 */

public class MaceKill extends Module {

    private final BooleanSetting max = sgGeneral.add(new BooleanSetting(
            "máxima potencia",
            "simular caída desde el hueco con aire más cercano en un rango de 170 bloques",
            false
    ));

    private final NumberSetting height = sgGeneral.add(new NumberSetting(
            "altura",
            "altura de la que simular la caída",
            22,
            1,
            170,
            1
    )).visibility(() -> !max.getValue());

    private final BooleanSetting preventDeath = sgGeneral.add(new BooleanSetting(
            "prevenir muerte",
            "prevenir la muerte por daño de caída si el ataque fue bloqueado, cancelando los paquetes de movimiento",
            true
    ));

    public MaceKill() {
        super("estampaescrotos", "estampa escrotos ajenos", Category.COMBAT);
    }

    @EventListener
    @SuppressWarnings("ConstantConditions")
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (mc.player.getMainHandItem().getItem() == Items.MACE
                && event.getPacket() instanceof ServerboundAttackPacket(int entityId)) {

            try {
                if (mc.level.getEntity(entityId) instanceof LivingEntity target) {

                    if (preventDeath.getValue()
                            && (target.isBlocking()
                            || target.isInvulnerable()
                            || target.hasInfiniteMaterials()
                    )) {
                        return;
                    }

                    Vec3 previouspos = mc.player.position();
                    int blocks = getMaxHeightAbovePlayer();

                    int packetsRequired = (int) (double) Math.abs(blocks / 10);

                    if (packetsRequired > 20) {
                        packetsRequired = 1;
                    }

                    BlockPos gap1 = (mc.player.blockPosition().offset(0, blocks, 0));
                    BlockPos gap2 = (mc.player.blockPosition().offset(0, blocks + 1, 0));

                    if (isSafeBlock(gap1) && isSafeBlock(gap2)) {
                        if (blocks <= 22) {
                            if (mc.player.isPassenger()) {
                                for (int i = 0; i < 4; i++) {
                                    mc.player.connection.send(ServerboundMoveVehiclePacket.fromEntity(mc.player.getVehicle()));
                                }

                                double maxHeight = Math.min(mc.player.getVehicle().getY() + 22, mc.player.getVehicle().getY() + blocks);

                                mc.player.getVehicle().setPos(mc.player.getVehicle().getX(), maxHeight + blocks, mc.player.getVehicle().getZ());
                                mc.player.connection.send(ServerboundMoveVehiclePacket.fromEntity(mc.player.getVehicle()));

                                mc.player.getVehicle().setPos(previouspos);
                                mc.player.connection.send(ServerboundMoveVehiclePacket.fromEntity(mc.player.getVehicle()));
                            } else {
                                for (int i = 0; i < 4; i++) {
                                    mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(false, mc.player.horizontalCollision));
                                }

                                double maxHeight = Math.min(mc.player.getY() + 22, mc.player.getY() + blocks);

                                ServerboundMovePlayerPacket movePacket = new ServerboundMovePlayerPacket.Pos(mc.player.getX(), maxHeight, mc.player.getZ(), false, mc.player.horizontalCollision);
                                mc.player.connection.send(movePacket);

                                ServerboundMovePlayerPacket returnPacket = new ServerboundMovePlayerPacket.Pos(previouspos.x(), previouspos.y(), previouspos.z(), false, mc.player.horizontalCollision);
                                mc.player.connection.send(returnPacket);
                            }
                        } else {
                            if (mc.player.isPassenger()) {
                                for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                                    mc.player.connection.send(ServerboundMoveVehiclePacket.fromEntity(mc.player.getVehicle()));
                                }

                                mc.player.getVehicle().setPos(mc.player.getVehicle().getX(), mc.player.getVehicle().getY() + blocks, mc.player.getVehicle().getZ());

                                double maxHeight = mc.player.getVehicle().getY() + blocks;

                                mc.player.getVehicle().setPos(mc.player.getVehicle().getX(), maxHeight + blocks, mc.player.getVehicle().getZ());
                                mc.player.connection.send(ServerboundMoveVehiclePacket.fromEntity(mc.player.getVehicle()));

                                mc.player.getVehicle().setPos(previouspos);
                                mc.player.connection.send(ServerboundMoveVehiclePacket.fromEntity(mc.player.getVehicle()));
                            } else {
                                for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                                    mc.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(false, mc.player.horizontalCollision));
                                }

                                mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ(), false, mc.player.horizontalCollision));
                                ServerboundMovePlayerPacket movePacket = new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ(), false, mc.player.horizontalCollision);
                                mc.player.connection.send(movePacket);
                            }

                            if (mc.player.isPassenger()) {
                                mc.player.getVehicle().setPos(previouspos);
                                mc.player.connection.send(ServerboundMoveVehiclePacket.fromEntity(mc.player.getVehicle()));

                                mc.player.getVehicle().setPos(previouspos);
                                mc.player.connection.send(ServerboundMoveVehiclePacket.fromEntity(mc.player.getVehicle()));
                            } else {
                                double maxHeight = mc.player.getY() + blocks;

                                ServerboundMovePlayerPacket movePacket = new ServerboundMovePlayerPacket.Pos(mc.player.getX(), maxHeight, mc.player.getZ(), false, mc.player.horizontalCollision);
                                ServerboundMovePlayerPacket returnPacket = new ServerboundMovePlayerPacket.Pos(previouspos.x(), previouspos.y(), previouspos.z(), false, mc.player.horizontalCollision);

                                mc.player.connection.send(returnPacket);
                                mc.player.connection.send(movePacket);
                                mc.player.connection.send(returnPacket);
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
        BlockPos playerPos = mc.player.blockPosition();
        int maxHeight = playerPos.getY() + (max.getValue() ? 170 : height.getIntValue());

        for (int i = maxHeight; i > playerPos.getY(); i--) {
            BlockPos isopenair1 = new BlockPos(playerPos.getX(), i, playerPos.getZ());
            BlockPos isopenair2 = isopenair1.above(1);

            if (isSafeBlock(isopenair1) && isSafeBlock(isopenair2)) {
                return i - playerPos.getY();
            }
        }

        return 0;
    }

    private boolean isSafeBlock(BlockPos pos) {
        return mc.level.getBlockState(pos).canBeReplaced()
                && mc.level.getFluidState(pos).isEmpty()
                && !mc.level.getBlockState(pos).is(Blocks.POWDER_SNOW);
    }
}
