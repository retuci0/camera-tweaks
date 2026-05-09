package me.retucio.sputnik.module.modules.player;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.event.input.MouseScrollEvent;
import me.retucio.sputnik.event.render.Render3DEvent;
import me.retucio.sputnik.event.interact.UseItemEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.NetworkUtil;
import me.retucio.sputnik.util.render.RenderUtil;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;


public class AirPlace extends Module {

    private final SettingGroup sgScroll = addSg(new SettingGroup("scrolleo", true));
    private final SettingGroup sgRender = addSg(new SettingGroup("renderizado", true));


    // general

    private final NumberSetting range = sgGeneral.add(new NumberSetting(
            "rango",
            "distancia a la que colocar el bloque",
            4,
            1,
            8,
            1
    ));

    private final BooleanSetting placeNormallyIfInRange = sgGeneral.add(new BooleanSetting(
            "colocar con normalidad",
            "colocar con normalidad si ya se está mirando a un bloque en rango",
            true
    ));


    // scroll

    private final KeySetting scrollKey = sgScroll.add(new KeySetting(
            "tecla del scroll",
            "tecla a mantener para que el scroll funcione",
            GLFW.GLFW_KEY_LEFT_CONTROL
    ));

    private final NumberSetting scrollSens = sgScroll.add(new NumberSetting(
            "sens. del scroll",
            "sensibilidad de la rueda del scroll (0 para desactivar)",
            1,
            0,
            5,
            0.1
    ));


    // renderizado

    private final BooleanSetting outlines = sgRender.add(new BooleanSetting(
            "contorno",
            "renderizar contorno de la caja de prev.",
            true
    ));

    private final ColorSetting outlineColor = sgRender.add(new ColorSetting(
            "color del contorno",
            "color a utilizar al dibujar el contorno",
            Colors.withAlpha(Colors.mainColor, 150),
            false
    ));

    private final NumberSetting lineWidth = sgRender.add(new NumberSetting(
            "grosor de línea",
            "grosor de las líneas del contorno",
            1,
            0.1,
            5,
            0.1
    ));

    private final BooleanSetting filling = sgRender.add(new BooleanSetting(
            "relleno",
            "renderizar relleno de la caja de prev.",
            true
    ));

    private final ColorSetting fillingColor = sgRender.add(new ColorSetting(
            "color del relleno",
            "color a utilizar al dibujar el relleno",
            Colors.withAlpha(Colors.mainColor.brighter(), 100),
            false
    ));

    private HitResult result;
    private Integer rangeValue = null;

    public AirPlace() {
        super("colocar en aire", "te permite colocar bloques en el aire", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (rangeValue == null) rangeValue = range.getIntValue();
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getCameraEntity() == null) return;
        result = mc.getCameraEntity().pick(rangeValue, 0, false);
    }

    @EventListener
    private void onUseItem(UseItemEvent event) {
        if (!(result instanceof BlockHitResult bhr) || !(event.getStack().getItem() instanceof BlockItem)) return;
        if (mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.MISS && placeNormallyIfInRange.getValue()) return;

        Vec3 hitPos = Vec3.atCenterOf(bhr.getBlockPos());

        BlockHitResult result = new BlockHitResult(
                hitPos, mc.player.getMotionDirection().getOpposite(), bhr.getBlockPos(), false);
        if (mc.player.mayUseItemAt(bhr.getBlockPos(), bhr.getDirection().getOpposite(), event.getStack()) && range.isValid(rangeValue))
            NetworkUtil.interactBlock(result, event.getHand(), true);
    }

    @EventListener
    private void onMouseScroll(MouseScrollEvent event) {
        if (!scrollKey.isDown()) return;
        rangeValue += (int) Math.round((event.getVertical() * scrollSens.getValue()));
        event.cancel();
    }

    @EventListener
    private void onKey(KeyEvent event) {
        if (event.getKey() == scrollKey.getValue() && event.getAction() == GLFW.GLFW_RELEASE) {
            rangeValue = (int) Math.clamp(rangeValue, range.getMin(), range.getMax());
        }
    }

    @EventListener
    private void onRenderWorld(Render3DEvent event) {
        if (!(result instanceof BlockHitResult bhr)
                || (mc.hitResult != null
                && mc.hitResult.getType() != HitResult.Type.MISS)
                || !mc.level.getBlockState(bhr.getBlockPos()).canBeReplaced())
            return;

        if (outlines.getValue()) {
            RenderUtil.drawBlockOutline(event.getMatrices(), bhr.getBlockPos(), outlineColor.getValue(), lineWidth.getFloatValue(), false);
        }
        if (filling.getValue()) {
            RenderUtil.drawBlockFilled(event.getMatrices(), bhr.getBlockPos(), fillingColor.getValue(), false);
        }
    }
}
