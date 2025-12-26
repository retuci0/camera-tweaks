package me.retucio.camtweaks.mixin.accessor;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilScreen.class)
public interface AnvilScreenAccessor {

    @Accessor("nameField")
    TextFieldWidget getNameField();

    @Accessor("nameField")
    void setNameField(TextFieldWidget nameField);
}
