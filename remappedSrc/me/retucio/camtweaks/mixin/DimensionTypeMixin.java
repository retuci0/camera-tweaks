package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.TimeChanger;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin {

    @ModifyReturnValue(method = "getMoonPhase", at = @At("RETURN"))
    private int overrideMoonPhase(int original) {
        TimeChanger timeChanger = ModuleManager.INSTANCE.getModuleByClass(TimeChanger.class);
        if (timeChanger.isEnabled() && mc.world != null && !timeChanger.moonPhase.is(TimeChanger.MoonPhases.DEFAULT))
            return timeChanger.getMoonPhase();
        return original;
    }
}
