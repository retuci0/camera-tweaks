package me.retucio.sputnik.module.modules.camera;

import com.github.retucio.neutrino.Event;
import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.event.input.MouseClickEvent;
import me.retucio.sputnik.event.input.MouseScrollEvent;
import me.retucio.sputnik.event.interact.*;
import me.retucio.sputnik.event.network.ChunkOcclusionEvent;
import me.retucio.sputnik.event.network.DisconnectEvent;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.mixin.mixins.entity.EntityMixin;
import me.retucio.sputnik.mixin.mixins.io.KeyboardInputMixin;
import me.retucio.sputnik.mixin.mixins.player.MultiPlayerGameModeMixin;
import me.retucio.sputnik.mixin.mixins.render.*;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.KeyUtil;
import me.retucio.sputnik.util.MiscUtil;
import net.minecraft.client.CameraType;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

/** continúa en:
 * @see CameraMixin
 * @see ChunkBorderRendererMixin
 * @see MultiPlayerGameModeMixin
 * @see EntityMixin
 * @see GameRendererMixin
 * @see KeyboardInputMixin
 * @see LivingEntityRendererMixin
 * @see LevelRendererMixin
 *
 * @author retucio
 */

public class Freecam extends Module {

    // ajustes
    private final SettingGroup sgSpeed = addSg(new SettingGroup("velocidad", true));

    private final BooleanSetting toggleOnDamage = sgGeneral.add(new BooleanSetting("desactivar al recibir daño", "desactiva el módulo tras recibir daño", true));
    private final BooleanSetting reloadChunks = sgGeneral.add(new BooleanSetting("recargar chunks", "recargar chunks para arreglar el culling de las cuevas", true));
    public final BooleanSetting renderHands = sgGeneral.add(new BooleanSetting("manos visibles", "decide si se renderizan las manos mientras la cámara esté libre", true));
    public final BooleanSetting stayCrouching = sgGeneral.add(new BooleanSetting("mantenerse agachado", "mantener al jugador agachado tras entrar en el modo de cámara libre", false));
    private final BooleanSetting staticView = sgGeneral.add(new BooleanSetting("visión estática", "desactiva ajustes que muevan la cámara", true));
    private final BooleanSetting cancelActionPackets = sgGeneral.add(new BooleanSetting("cancelar paquetes", "evita flaggear el anticheat al interactuar con bloques / entidades", true));
    public final BooleanSetting blockOutlines = sgGeneral.add(new BooleanSetting("contorno de bloques", "mostrar el contorno de los bloques estando en el modo de cámara libre", true));

    private final NumberSetting speedSetting = sgSpeed.add(new NumberSetting(
            "velocidad", "velocidad de movimiento de la cámara",
            1, 0, 10, 0.2));

    private final KeySetting scrollKey = sgSpeed.add(new KeySetting("tecla del scroll", "tecla a mantener pulsada para cambiar velocidad con el scroll", GLFW.GLFW_KEY_LEFT_CONTROL));
    private final NumberSetting scrollSens = sgSpeed.add(new NumberSetting(
            "sensibilidad del scroll", "sensibilidad de la rueda del ratón para modificar la velocidad, 0 para desactivar",
            1, 0, 2, 0.1));


    private final Vector3d prevPos = new Vector3d();
    private float prevYaw, prevPitch;

    private final Vector3d pos = new Vector3d();
    private CameraType perspective;
    private double fovScale, speed;
    private boolean forward, backward, right, left, up, down, crouching, viewBobbing;
    private float yaw, pitch;

    public Freecam() {
        super("cámara libre",
                "permite a la cámara moverse independientemente del jugador. útil para explorar alrededores",
                Category.CAMERA,
                GLFW.GLFW_KEY_V);

        speedSetting.onUpdate(newSpeed -> speed = newSpeed);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        fovScale = mc.options.fovEffectScale().get();
        viewBobbing = mc.options.bobView().get();

        if (staticView.getValue()) {
            mc.options.fovEffectScale().set(0D);
            mc.options.bobView().set(false);
        }

        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();
        perspective = mc.options.getCameraType();

        speed = speedSetting.getValue();

        MiscUtil.copyVector(pos, mc.gameRenderer.getMainCamera().position());
        MiscUtil.copyVector(prevPos, mc.gameRenderer.getMainCamera().position());

        if (mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
            yaw += 180;
            pitch *= -1;
        }

        prevYaw = yaw;
        prevPitch = pitch;

        crouching = mc.options.keyShift.isDown();

        forward = KeyUtil.isKeyDown(mc.options.keyUp);
        backward = KeyUtil.isKeyDown(mc.options.keyDown);
        right = KeyUtil.isKeyDown(mc.options.keyRight);
        left = KeyUtil.isKeyDown(mc.options.keyLeft);
        up = KeyUtil.isKeyDown(mc.options.keyJump);
        down = KeyUtil.isKeyDown(mc.options.keyShift);

        unpress();
        if (reloadChunks.getValue()) mc.levelRenderer.allChanged();

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (reloadChunks.getValue()) mc.execute(mc.levelRenderer::allChanged);

        mc.options.setCameraType(perspective);
        crouching = false;

        if (staticView.getValue()) {
            mc.options.fovEffectScale().set(fovScale);
            mc.options.bobView().set(viewBobbing);
        }

        // para no flaggear el anticheat, cancelar cualquier acción dejada a medias estando en modo libre
        if (cancelActionPackets.getValue()) {
            if (mc.gameMode != null) {
                mc.gameMode.stopDestroyBlock();
            }

            mc.options.keyAttack.setDown(false);
            mc.options.keyUse.setDown(false);
        }

        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.getCameraEntity() == null || perspective == null) return;

        mc.getCameraEntity().noPhysics = mc.getCameraEntity().isInWall();
        if (!perspective.isFirstPerson()) mc.options.setCameraType(CameraType.FIRST_PERSON);

        Vec3 forward = Vec3.directionFromRotation(0, yaw);
        Vec3 right = Vec3.directionFromRotation(0, yaw + 90);
        double dx = 0, dy = 0, dz = 0;

        double speedMultiplier = speed * (KeyUtil.isKeyDown(mc.options.keySprint) ? 1 : 0.5);

        boolean zMovement = false;
        if (this.forward) {
            dx += forward.x * speedMultiplier;
            dz += forward.z * speedMultiplier;
            zMovement = true;
        } if (this.backward) {
            dx -= forward.x * speedMultiplier;
            dz -= forward.z * speedMultiplier;
            zMovement = true;
        }

        boolean xMovement = false;
        if (this.right) {
            dx += right.x * speedMultiplier;
            dz += right.z * speedMultiplier;
            xMovement = true;
        } if (this.left) {
            dx -= right.x * speedMultiplier;
            dz -= right.z * speedMultiplier;
            xMovement = true;
        }

        if (xMovement && zMovement) {  // movimiento diagonal, teorema de Pitágoras
            dx /= Math.sqrt(2);
            dz /= Math.sqrt(2);
        }

        if (this.up) dy += speedMultiplier;
        if (this.down) dy -= speedMultiplier;

        prevPos.set(pos);
        pos.set(pos.x + dx, pos.y + dy, pos.z + dz);
    }

    @EventListener
    private void onSendPacket(PacketEvent.Send event) {
        if (!isEnabled() || !cancelActionPackets.getValue()) return;
        if (event.getStage() != Event.Stage.PRE) return;

        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundPlayerActionPacket
                || packet instanceof ServerboundUseItemOnPacket
                || packet instanceof ServerboundUseItemPacket
                || packet instanceof ServerboundSwingPacket) {
            event.cancel();
        }
    }

    @EventListener
    private void onKey(KeyEvent event) {
        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_F3)) return;
        if (mc.screen != null) return;

        net.minecraft.client.input.KeyEvent key = new net.minecraft.client.input.KeyEvent(event.getKey(), event.getScancode(), 0);

        boolean shouldCancel = true;
        if (mc.options.keyUp.matches(key)) {
            forward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyUp.setDown(false);
        } else if (mc.options.keyDown.matches(key)) {
            backward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyDown.setDown(false);
        } else if (mc.options.keyRight.matches(key)) {
            right = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyRight.setDown(false);
        } else if (mc.options.keyLeft.matches(key)) {
            left = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyLeft.setDown(false);
        } else if (mc.options.keyJump.matches(key)) {
            up = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyJump.setDown(false);
        } else if (mc.options.keyShift.matches(key)) {
            down = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyShift.setDown(false);
        } else {
            shouldCancel = false;
        }

        if (shouldCancel) event.cancel();
    }

    @EventListener
    private void onMouseClick(MouseClickEvent event) {  // por si el restrasado del usuario usa el ratón para moverse
        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_F3)) return;
        if (mc.screen != null) return;

        MouseButtonEvent click = new MouseButtonEvent(0, 0, new MouseButtonInfo(event.getButton(), 0));

        boolean shouldCancel = true;
        if (mc.options.keyUp.matchesMouse(click)) {
            forward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyUp.setDown(false);
        } else if (mc.options.keyDown.matchesMouse(click)) {
            backward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyDown.setDown(false);
        } else if (mc.options.keyRight.matchesMouse(click)) {
            right = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyRight.setDown(false);
        } else if (mc.options.keyLeft.matchesMouse(click)) {
            left = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyLeft.setDown(false);
        } else if (mc.options.keyJump.matchesMouse(click)) {
            up = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyJump.setDown(false);
        } else if (mc.options.keyShift.matchesMouse(click)) {
            down = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.keyShift.setDown(false);
        } else {
            shouldCancel = false;
        }

        if (shouldCancel) event.cancel();
    }

    @EventListener
    private void onMouseScroll(MouseScrollEvent event) {
        if (scrollSens.getValue() > 0 && mc.screen == null && scrollKey.isDown()) {
            speed += event.getVertical() / 4 * (scrollSens.getValue() * speed);
            if (speed < 0.1) speed = 0.1;
            event.cancel();
        }
    }

    @EventListener
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventListener
    private void onLeaveGame(DisconnectEvent event) {
        toggle();
    }

    @EventListener
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;  // el jugador probablemente nunca sea nulo bajo estas circunstancias pero quién sabe

        if (event.getPacket() instanceof ClientboundPlayerCombatKillPacket packet) {
            if (mc.player.getId() == packet.playerId())
                toggle();
        } else if (event.getPacket() instanceof ClientboundSetHealthPacket packet) {
            if (mc.player.getHealth() - packet.getHealth() > 0 && toggleOnDamage.getValue()) {
                ChatUtil.info("cámara libre se ha desactivado porque has recibido daño");
                toggle();
            }
        }
    }

    @EventListener
    private void onOpenScreen(OpenScreenEvent event) {
        unpress();
        stopMoving();
        prevPos.set(pos);
        prevYaw = yaw;
        prevPitch = pitch;
    }

    // los paquetes ya se cancelan, pero visualmente estos eventos se siguen dando

    @EventListener
    private void onBreakBlock(BreakBlockEvent event) {
        if (cancelActionPackets.getValue()) event.cancel();
    }

    @EventListener
    private void onPlaceBlock(PlaceBlockEvent event) {
        if (cancelActionPackets.getValue()) event.cancel();
    }

    @EventListener
    private void onInteractEntity(InteractEntityEvent event) {
        if (cancelActionPackets.getValue()) event.cancel();
    }

    @EventListener
    private void onAttack(AttackEntityEvent event) {
        if (cancelActionPackets.getValue()) event.cancel();
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        prevYaw = yaw;
        prevPitch = pitch;

        yaw += (float) deltaX;
        pitch += (float) deltaY;

        pitch = Mth.clamp(pitch, -90, 90);
    }

    private void unpress() {
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keyShift.setDown(false);
    }

    private void stopMoving() {
        forward = false;
        backward = false;
        left = false;
        right = false;
        up = false;
        down = false;
    }


    // getters

    public Vector3d getPos() {
        return pos;
    }

    public Vector3d getPrevPos() {
        return prevPos;
    }

    public float getYaw() {
        return yaw;
    }

    public float getYaw(float tickDelta) {
        return Mth.lerp(tickDelta, prevYaw, yaw);
    }

    public float getPitch() {
        return pitch;
    }

    public float getPitch(float tickDelta) {
        return Mth.lerp(tickDelta, prevPitch, pitch);
    }

    public float getPrevYaw() {
        return prevYaw;
    }

    public float getPrevPitch() {
        return prevPitch;
    }

    public double getX(float tickDelta) {
        return Mth.lerp(tickDelta, prevPos.x, pos.x);
    }

    public double getY(float tickDelta) {
        return Mth.lerp(tickDelta, prevPos.y, pos.y);
    }

    public double getZ(float tickDelta) {
        return Mth.lerp(tickDelta, prevPos.z, pos.z);
    }

    public boolean isCrouching() {
        return crouching;
    }
}
