package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.Nametags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Text addHealthIndicator(Text original) {
        Nametags nametags = ModuleManager.INSTANCE.getModuleByClass(Nametags.class);
        if (!nametags.isEnabled() || !nametags.health.isEnabled()) return original;

        float health = getHealth();
        float absorption = getAbsorptionAmount();
        Formatting color;

        if (health < 5) color = Formatting.RED;
        else if (health < 10) color = Formatting.YELLOW;
        else if (health < 15) color = Formatting.GREEN;
        else color = Formatting.DARK_GREEN;

        // me gustaría también añadir un modo en el que se renderizan los corazones directamente en la nametag, pero no sé cómo hacer para las imágenes :'(
        // ahora que lo pienso en la snapshot más reciente han metido iconos que se pueden usar en texto, y uno de ellos es un corazón :D
        String text = " [" + color +
                (nametags.healthMode.is(Nametags.HealthMode.HEARTS)
                        ? String.format("%.2f", health / 2)  // redondear a dos decimales
                        : (int) health) +
                (absorption > 0  // tener absorción en cuenta
                        ? Formatting.RESET + " + " + Formatting.GOLD +
                        (nametags.healthMode.is(Nametags.HealthMode.HEARTS)
                                ? String.format("%.2f", absorption / 2)
                                : Integer.toString((int) absorption))
                        : "") +
                Formatting.RESET + "]";

        return original.copy().append(text);
    }
}