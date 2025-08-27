package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.*;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.KeyUtil;
import me.retucio.camtweaks.util.MiscUtil;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.CameraMixin
 * @see me.retucio.camtweaks.mixin.ChunkBorderDebugRendererMixin
 * @see me.retucio.camtweaks.mixin.CompassStateMixin
 * @see me.retucio.camtweaks.mixin.EntityMixin
 * @see me.retucio.camtweaks.mixin.GameRendererMixin
 * @see me.retucio.camtweaks.mixin.KeyInputMixin
 * @see me.retucio.camtweaks.mixin.LivingEntityRendererMixin
 * @see me.retucio.camtweaks.mixin.WorldRendererMixin
 */

public class Freecam extends Module {

    // ajustes

    public BooleanSetting toggleOnDamage = addSetting(new BooleanSetting("desactivar al recibir daño", "desactiva el módulo tras recibir daño", true));
    public BooleanSetting reloadChunks = addSetting(new BooleanSetting("recargar chunks", "recargar chunks para arreglar el culling de las cuevas", true));
    public BooleanSetting renderHands = addSetting(new BooleanSetting("manos visibles", "decide si se renderizan las manos mientras la cámara esté libre", true));
    public BooleanSetting stayCrouching = addSetting(new BooleanSetting("mantenerse agachado", "mantener al jugador agachado tras entrar en el modo de cámara libre", false));
    public BooleanSetting staticView = addSetting(new BooleanSetting("visión estática", "desactiva ajustes que muevan la cámara", true));
    public BooleanSetting cancelActionPackets = addSetting(new BooleanSetting("cancelar paquetes", "evita flaggear el anticheat al interactuar con bloques / entidades", true));

    public NumberSetting speedSetting = addSetting(new NumberSetting(
            "velocidad", "velocidad de movimiento de la cámara",
            1, 0, 10, 0.2));
    public NumberSetting scrollSens = addSetting(new NumberSetting(
            "sensibilidad del scroll", "sensibilidad de la rueda del ratón para modificar la velocidad, 0 para desactivar",
            0, 0, 2, 0.1));


    // rotate?

    private final Vector3d prevPos = new Vector3d();
    private float prevYaw, prevPitch;

    private final Vector3d pos = new Vector3d();
    private Perspective perspective;
    private double fovScale, speed;
    private boolean forward, backward, right, left, up, down, crouching, viewBobbing;
    private float yaw, pitch;

    public Freecam() {
        super("cámara libre", "perrmite a la cámara moverse independientemente del jugador. útil para explorar alrededores");
        assignKey(GLFW.GLFW_KEY_V);
        speedSetting.onUpdate(newSpeed -> speed = newSpeed);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.options == null) return;

        fovScale = mc.options.getFovEffectScale().getValue();
        viewBobbing = mc.options.getBobView().getValue();

        if (staticView.isEnabled()) {
            mc.options.getFovEffectScale().setValue(0D);
            mc.options.getBobView().setValue(false);
        }

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        perspective = mc.options.getPerspective();

        speed = speedSetting.getValue();

        MiscUtil.copyVector(pos, mc.gameRenderer.getCamera().getCameraPos());
        MiscUtil.copyVector(prevPos, mc.gameRenderer.getCamera().getCameraPos());

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            yaw += 180;
            pitch *= -1;
        }

        prevYaw = yaw;
        prevPitch = pitch;

        crouching = mc.options.sneakKey.isPressed();

        forward = KeyUtil.isKeyDown(mc.options.forwardKey);
        backward = KeyUtil.isKeyDown(mc.options.backKey);
        right = KeyUtil.isKeyDown(mc.options.rightKey);
        left = KeyUtil.isKeyDown(mc.options.leftKey);
        up = KeyUtil.isKeyDown(mc.options.jumpKey);
        down = KeyUtil.isKeyDown(mc.options.sneakKey);

        unpress();
        if (reloadChunks.isEnabled()) mc.worldRenderer.reload();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (reloadChunks.isEnabled()) mc.execute(mc.worldRenderer::reload);

        mc.options.setPerspective(perspective);
        crouching = false;

        if (staticView.isEnabled()) {
            mc.options.getFovEffectScale().setValue(fovScale);
            mc.options.getBobView().setValue(viewBobbing);
        }

        // para no flaggear el anticheat, cancelar cualquier acción dejada a medias estando en modo libre
        if (cancelActionPackets.isEnabled()) {
            if (mc.interactionManager != null)
                mc.interactionManager.cancelBlockBreaking();

            if (mc.options != null) {
                mc.options.attackKey.setPressed(false);
                mc.options.useKey.setPressed(false);
            }
        }

        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.cameraEntity == null || perspective == null) return;

        mc.cameraEntity.noClip = mc.cameraEntity.isInsideWall();
        if (!perspective.isFirstPerson()) mc.options.setPerspective(Perspective.FIRST_PERSON);

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double dx = 0, dy = 0, dz = 0;

        double speedMultiplier = speed * (KeyUtil.isKeyDown(mc.options.sprintKey) ? 1 : 0.5);

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

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (!isEnabled() || !cancelActionPackets.isEnabled()) return;

        Packet<?> p = event.getPacket();
        if (p instanceof PlayerActionC2SPacket
                || p instanceof PlayerInteractBlockC2SPacket
                || p instanceof PlayerInteractItemC2SPacket
                || p instanceof HandSwingC2SPacket) {
            event.cancel();
        }
    }

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        if (mc.currentScreen != null) return;
        if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_F3)) return; // ?

        boolean shouldCancel = true;
        if (mc.options.forwardKey.matchesKey(event.getKey(), event.getScancode())) {
            forward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.forwardKey.setPressed(false);
        } else if (mc.options.backKey.matchesKey(event.getKey(), event.getScancode())) {
            backward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.backKey.setPressed(false);
        } else if (mc.options.rightKey.matchesKey(event.getKey(), event.getScancode())) {
            right = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.rightKey.setPressed(false);
        } else if (mc.options.leftKey.matchesKey(event.getKey(), event.getScancode())) {
            left = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.leftKey.setPressed(false);
        } else if (mc.options.jumpKey.matchesKey(event.getKey(), event.getScancode())) {
            up = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.jumpKey.setPressed(false);
        } else if (mc.options.sneakKey.matchesKey(event.getKey(), event.getScancode())) {
            down = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.sneakKey.setPressed(false);
        } else {
            shouldCancel = false;
        }

        if (shouldCancel) event.cancel();
    }

    @SubscribeEvent
    public void onMouseClick(MouseClickEvent event) {  // por si el restrasado del usuario usa el ratón para moverse
        if (mc.currentScreen != null) return;

        boolean shouldCancel = true;
        if (mc.options.forwardKey.matchesKey(event.getButton(), 0)) {
            forward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.forwardKey.setPressed(false);
        } else if (mc.options.backKey.matchesKey(event.getButton(), 0)) {
            backward = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.backKey.setPressed(false);
        } else if (mc.options.rightKey.matchesKey(event.getButton(), 0)) {
            right = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.rightKey.setPressed(false);
        } else if (mc.options.leftKey.matchesKey(event.getButton(), 0)) {
            left = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.leftKey.setPressed(false);
        } else if (mc.options.jumpKey.matchesKey(event.getButton(), 0)) {
            up = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.jumpKey.setPressed(false);
        } else if (mc.options.sneakKey.matchesKey(event.getButton(), 0)) {
            down = event.getAction() != GLFW.GLFW_RELEASE;
            mc.options.sneakKey.setPressed(false);
        } else {
            shouldCancel = false;
        }

        if (shouldCancel) event.cancel();
    }

    @SubscribeEvent
    private void onMouseScroll(MouseScrollEvent event) {
        if (scrollSens.getValue() > 0 && mc.currentScreen == null) {
            speed += event.getVertical() / 4 * (scrollSens.getValue() * speed);
            if (speed < 0.1) speed = 0.1;
            event.cancel();
        }
    }

    @SubscribeEvent
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @SubscribeEvent
    public void onLeaveGame(DisconnectEvent event) {
        toggle();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;  // el jugador probablemente nunca sea nulo bajo estas circunstancias pero quién sabe

        if (event.getPacket() instanceof DeathMessageS2CPacket packet) {
            if (mc.player.getId() == packet.playerId())
                toggle();
        } else if (event.getPacket() instanceof HealthUpdateS2CPacket packet) {
            if (mc.player.getHealth() - packet.getHealth() > 0 && toggleOnDamage.isEnabled()) {
                ChatUtil.info("cámara libre se ha desactivado porque has recibido daño");
                toggle();
            }
        }
    }

    @SubscribeEvent
    public void onOpenScreen(OpenScreenEvent event) {
        unpress();
        prevPos.set(pos);
        prevYaw = yaw;
        prevPitch = pitch;
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        prevYaw = yaw;
        prevPitch = pitch;

        yaw += (float) deltaX;
        pitch += (float) deltaY;

        pitch = MathHelper.clamp(pitch, -90, 90);
    }

    private void unpress() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
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
        return MathHelper.lerp(tickDelta, prevYaw, yaw);
    }

    public float getPitch() {
        return pitch;
    }

    public float getPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPitch, pitch);
    }

    public float getPrevYaw() {
        return prevYaw;
    }

    public float getPrevPitch() {
        return prevPitch;
    }

    public double getX(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.x, pos.x);
    }

    public double getY(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.y, pos.y);
    }

    public double getZ(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.z, pos.z);
    }

    public boolean isCrouching() {
        return crouching;
    }
}
