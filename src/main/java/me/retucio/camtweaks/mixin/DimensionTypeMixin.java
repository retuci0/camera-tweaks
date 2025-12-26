package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.TimeChanger;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin {

    // no consigo arreglar lo de que la fase lunar por defecto siempre sea luna llena
    @ModifyReturnValue(method = "getMoonPhase", at = @At("RETURN"))
    private int overrideMoonPhase(int original, @Local(argsOnly = true) long time) {
        TimeChanger timeChanger = ModuleManager.INSTANCE.getModuleByClass(TimeChanger.class);
        if (timeChanger == null || !timeChanger.isEnabled() || mc.world == null) {
            return original;
        }

        if (timeChanger.moonPhase.is(TimeChanger.MoonPhases.DEFAULT))
            return (int) (time / 24000L % 8L + 8L) % 8;

        return timeChanger.getMoonPhase();
    }
}
